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

import nl.kpmg.lcm.common.data.ContentIterator;
import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.DataState;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;
import nl.kpmg.lcm.server.data.service.StorageService;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.MetaModelException;
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
import java.util.LinkedList;
import java.util.List;
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
  protected void enrichMetadataItem(EnrichmentProperties properties, String key) throws IOException {
    File dataSourceFile = createDataSourceFile(key);
    jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).clearDetailsDescriptor();
    if (properties.getAccessibility()) {
      String state = dataSourceFile.exists() ? DataState.ATTACHED : DataState.DETACHED;
      jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
          .setState(state);
    }

    if (dataSourceFile.exists()) {
      if (properties.getSize()) {
        jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setSize(dataSourceFile.length());
      }
      if (properties.getStructure()) {
        JsonDataContext dataContext = createDataContext(dataSourceFile);
        try {
          Schema schema = dataContext.getDefaultSchema();
          if (schema.getTableCount() == 0) {
            return;
          }
          Table table = schema.getTables()[0];
          jsonMetaData.getTableDescription(key).setColumns(table.getColumns());
        } catch (MetaModelException mme) {
          String state = DataState.INVALID;
          jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
              .setState(state);
          LOGGER.warn("The metadata with id: " + jsonMetaData.getId()
              + " describes invalid data. Invalid data key: " + key);
        }
      }
      Long dataUpdateTime = new Date(dataSourceFile.lastModified()).getTime();
      jsonMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
          .setDataUpdateTimestamp(dataUpdateTime);
    }
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
      if (progressIndicationFactory != null) {
        String message = "The file: " + dataSourceFile.getPath() + " is already attached, won't overwrite.";
        progressIndicationFactory.writeIndication(message);
      }
      throw new LcmException("Data set is already attached, won't overwrite. Data item: " + key);
    }

    int rowNumber = 1;
    Gson gson = new Gson();
    try (Writer writer = FileHelper.getBufferedWriter(dataSourceFile);) {
      if (progressIndicationFactory != null) {
        String message = "Start transfer. File: "+ dataSourceFile.getPath();
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

  @Override
  protected List loadDataItems(String storageName, String subPath) {

    Storage storage = storageService.findByName(storageName);
    LocalFileStorage fileStorage = new LocalFileStorage(storage);
    String storagePath = fileStorage.getStoragePath();
    File dataSourceDir = new File(storagePath + "/" +  subPath);

    if (!dataSourceDir.exists() || !dataSourceDir.isDirectory()) {
      String message =
          String.format("The storage %s is pointing non existing directory %s", storageName,
              subPath);
      throw new LcmException(message);
    }

    File[] files = dataSourceDir.listFiles();
    List<String> fileNameList = new LinkedList();
    for (File file : files) {
      if (file.isFile()) {
        fileNameList.add(file.getName());
      }
    }

    return fileNameList;
  }

}