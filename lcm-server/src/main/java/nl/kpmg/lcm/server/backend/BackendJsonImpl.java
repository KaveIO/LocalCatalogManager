/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.backend;
import com.google.gson.Gson;

import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.server.data.IterativeData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.TransferSettings;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.json.JsonDataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = {DataFormat.JSON})
public class BackendJsonImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendJsonImpl.class.getName());
  //private File dataSourceFile = null;
  private final TabularMetaData jsonMetaData;

  /**
   *
   * @param backendStorage valid storage. This storage name must be extracted from @metaData object
   *        and then the storage object loaded be loaded .
   * @param metaData - valid @metaData that representing CSV data source.
   */
  public BackendJsonImpl(MetaData metaData, StorageService storageService) {
    super(metaData, storageService);
    this.jsonMetaData = new TabularMetaData(metaData);

  }

  private JsonDataContext createDataContext(File dataSourceFile) {
    if (jsonMetaData == null) {
      throw new IllegalStateException("MetaData parameter could not be null");
    }

    if (!dataSourceFile.exists()) {
      throw new LcmException("Unable to find data source file! FilePath: "
          + dataSourceFile.getPath());
    }
    return (JsonDataContext) DataContextFactory.createJsonDataContext(dataSourceFile);
  }

  private File createDataSourceFile(String key) {
    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    String filePath = storageService.getStorageItemName(dataURI);
    Storage storage = storageService.getStorageByUri(dataURI);        
    String storagePath = new LocalFileStorage(storage).getStoragePath();
    
    File baseDir = new File(storagePath);
    File dataSourceFile = new File(storagePath + filePath);

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, dataSourceFile, notification);

    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }

    return dataSourceFile;
  }

  @Override
  public MetaData enrichMetadata(EnrichmentProperties properties) {
    expandDataURISection();
    if (metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors() == null) {
      return metaDataWrapper.getMetaData();
    }
    for (String key : metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors().keySet()) {
    File dataSourceFile = createDataSourceFile(key);
    long start = System.currentTimeMillis();
    try {
      jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).clearDetailsDescriptor();
      if (properties.getAccessibility()) {
        String state = dataSourceFile.exists() ? "ATTACHED" : "DETACHED";
        jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setState(state);
      }

      if (dataSourceFile.exists()) {
        if (properties.getSize()) {
          jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setSize(dataSourceFile.length());
        }
        if (properties.getStructure()) {
          JsonDataContext dataContext = createDataContext(dataSourceFile);
          Schema schema = dataContext.getDefaultSchema();
          if (schema.getTableCount() == 0) {
            return null;
          }
          Table table = schema.getTables()[0];
          jsonMetaData.getTableDescription(key).setColumns(table.getColumns());
        }
        Long dataUpdateTime = new Date(dataSourceFile.lastModified()).getTime();
        jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setDataUpdateTimestamp(dataUpdateTime);
      }
    } catch (Exception ex) {
      LOGGER.error("Unable to enrich medatadata : " + jsonMetaData.getId() + ". Error Message: "
          + ex.getMessage());
      throw new LcmException("Unable to get info about datasource: " + dataSourceFile.getPath(), ex);
    } finally {
      jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setUpdateTimestamp(new Date().getTime());
      long end = System.currentTimeMillis();
      jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setUpdateDurationTimestamp(end - start);
    }
    }

    return metaDataWrapper.getMetaData();
  }

  /**
   * Method to store some content on a data storage backend.
   *
   * @param content {@link ContentIterator} that should be stored.
   * @param forceOverwrite - indicates how to proceed if the content already exists - in case of
   *        true the content is written no matter if already persists or not - in case it is set to
   *        false then the content is written only when it doesn't exist - in case it is set to
   *        false and the content already exists then LcmException is thrown.
   */
  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {

    if (!(data instanceof IterativeData)) {
      throw new LcmException("Unable to store streaming data directly to json.");
    }

    File dataSourceFile = createDataSourceFile(key);
    
    ContentIterator content = ((IterativeData) data).getIterator();
    if (dataSourceFile.exists() && !transferSettings.isForceOverwrite()) {
      throw new LcmException("Data set is already attached, won't overwrite. Data item: " + key);
    }

    int rowNumber = 1;
    Gson gson = new Gson();
    try (Writer writer = FileHelper.getBufferedWriter(dataSourceFile);) {
      if (progressIndicationFactory != null) {
        String message = "Start transfer. File: "+ dataSourceFile.getName();
        progressIndicationFactory.writeIndication(message);
      }
      while (content.hasNext()) {
        gson.toJson(content.next(), Map.class, writer);
        rowNumber++;
        if (progressIndicationFactory != null
            && rowNumber % progressIndicationFactory.getIndicationChunkSize() == 0) {
          String message = "Written " + (rowNumber - 1) + " records!";
          progressIndicationFactory.writeIndication(message);
        }
      }
      writer.flush();
      String message = "Written successfully all the records: " + (rowNumber - 1);
      if (progressIndicationFactory != null) {
        progressIndicationFactory.writeIndication(message);
      }
    } catch (IOException ex) {
      LOGGER.error("Error occured during saving information!", ex);
      if (progressIndicationFactory != null) {
        String message =
            String.format("The content is inserted partially, only %d rows in file: %s",
                (rowNumber - 1), dataSourceFile.getName());
        progressIndicationFactory.writeIndication(message);
      }
    }
  }

  private String[] toStringArray(Object[] lineAsObjectValues) {
    String[] lineAsStringValues = new String[lineAsObjectValues.length];
    for (int i = 0; i < lineAsObjectValues.length; i++) {
      lineAsStringValues[i] = lineAsObjectValues[i].toString();
    }
    return lineAsStringValues;
  }

  /**
   * Method to read some content from a data storage backend.
   *
   * @return {@link DataSet} with all the data specified in the @metaData object passed during
   *         initialization.
   */
  @Override
  public IterativeData read(String key) {
    File dataSourceFile = createDataSourceFile(key);

    JsonDataContext dataContext = createDataContext(dataSourceFile);
    Schema schema = dataContext.getDefaultSchema();
    if (schema.getTableCount() == 0) {
      return null;
    }
    Table table = schema.getTables()[0];
    DataSet result = dataContext.query().from(table).selectAll().execute();
    jsonMetaData.getTableDescription(key).setColumns(table.getColumns());
    return new IterativeData(new DataSetContentIterator(result));
  }

  @Override
  public boolean delete(String key) {
    throw new UnsupportedOperationException("Backend delete operation is not supported yet.");
  }

  @Override
  public void free() {

  }
}