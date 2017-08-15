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

import nl.kpmg.lcm.server.backend.metadata.CsvMetaData;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;
import nl.kpmg.lcm.common.data.ContentIterator;
import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = {DataFormat.CSV})
public class BackendCsvImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendCsvImpl.class.getName());
  private final CsvMetaData csvMetaData;


  /**
   *
   * @param storageService valid StorageService instance
   * @param metaData - valid @metaData that representing CSV data source.
   */
  public BackendCsvImpl(MetaData metaData, StorageService storageService) {
    super(metaData, storageService);
    this.csvMetaData = new CsvMetaData(metaData);
  }

  private UpdateableDataContext createDataContext(File dataSourceFile, String key) {
    if (metaDataWrapper == null) {
      throw new IllegalStateException("MetaData parameter could not be null");
    }

    CsvConfiguration csvConfiguration = null;
    if(csvMetaData.doesConfigurationExists(key)){
        csvConfiguration =  csvMetaData.getConfiguration(key);
    } else {
        csvConfiguration =  csvMetaData.getDefaultConfiguration();
    }

    if (!dataSourceFile.exists()) {
      throw new LcmException("Unable to find data source file! FilePath: "
          + dataSourceFile.getPath());
    }
    return (CsvDataContext) DataContextFactory.createCsvDataContext(dataSourceFile,
        csvConfiguration);
  }

  private File constructDatasourceFile(String key) {
    DataItemsDescriptor dynamicDataDescriptor =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
    String unparesURI = dynamicDataDescriptor.getURI();
    URI parsedUri;
    try {
      parsedUri = new URI(unparesURI);
    } catch (URISyntaxException ex) {
      LOGGER.error("unable to parse uri " + unparesURI + " for key:" + key);
      return null;
    }

    String storageName =
        parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
    Storage storage = storageService.findByName(storageName);

    if (storage == null) {
      LOGGER.error("Storage with name: " + storageName + " does not exists! Data item with key "
          + key + "will not be updated");
      return null;
    }
    LocalFileStorage localStorage = new LocalFileStorage(storage);
    String fileName = parsedUri.getPath();

    File dataSourceFile;
    File baseDir = new File(localStorage.getStoragePath());

    dataSourceFile = new File(localStorage.getStoragePath() + fileName);

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

      DataItemsDescriptor dynamicDataDescriptor =
          metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
      File dataSourceFile = constructDatasourceFile(key);
      long start = System.currentTimeMillis();
      try {
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).clearDetailsDescriptor();
        if (properties.getAccessibility()) {
          String state = dataSourceFile.exists() ? "ATTACHED" : "DETACHED";
          dynamicDataDescriptor.getDetailsDescriptor().setState(state);
        }

        if (dataSourceFile.exists()) {
          if (properties.getSize()) {
            dynamicDataDescriptor.getDetailsDescriptor().setSize(dataSourceFile.length());
          }
          if (properties.getStructure()) {
            UpdateableDataContext dataContext = createDataContext(dataSourceFile, key);
            Schema schema = dataContext.getDefaultSchema();
            if (schema.getTableCount() == 0) {
              return null;
            }
            Table table = schema.getTables()[0];
            csvMetaData.getTableDescription(key).setColumns(table.getColumns());
          }
          Long dataUpdateTime = new Date(dataSourceFile.lastModified()).getTime();
          dynamicDataDescriptor.getDetailsDescriptor().setDataUpdateTimestamp(dataUpdateTime);
        }
      } catch (Exception ex) {
        LOGGER.error("Unable to enrich medatadata : " + metaDataWrapper.getId() + ". Error Message: "
            + ex.getMessage());
        throw new LcmException("Unable to get info about datasource: " + dataSourceFile.getPath(),
            ex);
      } finally {
        dynamicDataDescriptor.getDetailsDescriptor().setUpdateTimestamp(new Date().getTime());
        long end = System.currentTimeMillis();
        dynamicDataDescriptor.getDetailsDescriptor().setUpdateDurationTimestamp(end - start);
      }
    }

    return metaDataWrapper.getMetaData();
  }

  /**
   * Method to store some content on a data storage backend.
   *
   * @param data {@link ContentIterator} that should be stored.
   * @param transferSettings - settings how to deal with special cases as:
   * -  already existing data
   * - 
   */
  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {

    if (!(data instanceof IterativeData)) {
      throw new LcmException("Unable to store streaming data directly to csv.");
    }
    
    ContentIterator content = ((IterativeData) data).getIterator();
    File dataSourceFile = constructDatasourceFile(key);
    if (dataSourceFile.exists() && !transferSettings.isForceOverwrite()) {
      throw new LcmException("Data set is already attached, won't overwrite. Data item: " + key);
    }

    int rowNumber = 1;
    try (Writer writer = FileHelper.getBufferedWriter(dataSourceFile);) {

      CsvConfiguration configuration = csvMetaData.getConfiguration(key);
      CsvWriter csvWriter = new CsvWriter(configuration);

      if (progressIndicationFactory != null) {
        String message = "Start transfer. File: " + dataSourceFile.getName();
        progressIndicationFactory.writeIndication(message);
      }
      while (content.hasNext()) {

        Map row = content.next();

        if (rowNumber == configuration.getColumnNameLineNumber()) {
          Object[] lineAsObjectValues = (Object[]) row.keySet().toArray(new Object[] {});
          String[] lineAsStringValues = toStringArray(lineAsObjectValues);
          String columnLine = csvWriter.buildLine(lineAsStringValues);
          writer.write(columnLine);
          rowNumber++;
        }

        Object[] lineAsObjectValues = (Object[]) row.values().toArray(new Object[] {});
        String[] lineAsStringValues = toStringArray(lineAsObjectValues);
        String line = csvWriter.buildLine(lineAsStringValues);
        writer.write(line);
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
    File dataSourceFile = constructDatasourceFile(key);
    UpdateableDataContext dataContext = createDataContext(dataSourceFile, key);
    Schema schema = dataContext.getDefaultSchema();
    if (schema.getTableCount() == 0) {
      return null;
    }
    Table table = schema.getTables()[0];
    DataSet result = dataContext.query().from(table).selectAll().execute();
    csvMetaData.getTableDescription(key).setColumns(table.getColumns());

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
    File dataSourceDir = new File(storagePath + "/" + subPath);

    if (!dataSourceDir.exists() || !dataSourceDir.isDirectory()) {
      throw new LcmException("The storage is pointing non existing directory");
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
