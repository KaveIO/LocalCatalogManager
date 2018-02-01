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

import nl.kpmg.lcm.common.data.ContentIterator;
import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.DataState;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.exception.LcmExposableException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.backend.metadata.CsvMetaData;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;
import nl.kpmg.lcm.server.data.CsvAdapter;
import nl.kpmg.lcm.server.data.FileSystemAdapter;
import nl.kpmg.lcm.server.data.LocalCsvAdapter;
import nl.kpmg.lcm.server.data.LocalFileSystemAdapter;
import nl.kpmg.lcm.server.data.azure.AzureCsvAdapter;
import nl.kpmg.lcm.server.data.azure.AzureFileSystemAdapter;
import nl.kpmg.lcm.server.data.service.StorageService;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = {DataFormat.CSV, DataFormat.AZURECSV})
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

  @Override
  protected void enrichMetadataItem(EnrichmentProperties properties, String key) throws IOException {
    DataItemsDescriptor dynamicDataDescriptor =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
    CsvAdapter csvAdapter = null;
    try {
      csvAdapter = getCsvAdapter(key);
    } catch (LcmValidationException ex) {
      String state = DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
      LOGGER.warn("The metadata with id: " + csvMetaData.getId()
          + " has problems with storage validation. " + ex.getNotification().errorMessage());
      return;
    } catch (Exception ex) {
      String state = DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
      LOGGER.warn("Metadata id: " + csvMetaData.getId()
          + ". Error message: " + ex.getMessage());
      return;
    }
    metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).clearDetailsDescriptor();

    try {
      if (!csvAdapter.exists()) {
        String state = DataState.DETACHED;
        dynamicDataDescriptor.getDetailsDescriptor().setState(state);
        LOGGER.warn("The metadata with id: " + csvMetaData.getId()
            + " has storage directory which does not exist.");
        return;
      }
    } catch (Exception ex) {
      String state = DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
      LOGGER.warn("Metadata id: " + csvMetaData.getId() + " . Error message: "
          + ex.getMessage());
      return;
    }

    if (properties.getAccessibility()) {
      String state = csvAdapter.exists() ? DataState.ATTACHED : DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
    }

    if (properties.getSize()) {
      dynamicDataDescriptor.getDetailsDescriptor().setSize(csvAdapter.length());
    }
    if (properties.getStructure()) {
      UpdateableDataContext dataContext = createDataContext(csvAdapter.getInputStream(), key);
      try {
        Schema schema = dataContext.getDefaultSchema();
        if (schema.getTableCount() == 0) {
          return;
        }
        Table table = schema.getTables()[0];
        csvMetaData.getTableDescription(key).setColumns(table.getColumns());
      } catch (MetaModelException mme) {
        String state = DataState.INVALID;
        dynamicDataDescriptor.getDetailsDescriptor().setState(state);
        LOGGER.warn("The metadata with id: " + csvMetaData.getId()
            + " describes invalid data. Invalid data key: " + key);
      }
    }
    Long dataUpdateTime = new Date(csvAdapter.lastModified()).getTime();
    dynamicDataDescriptor.getDetailsDescriptor().setDataUpdateTimestamp(dataUpdateTime);
  }

  /**
   * Method to store some content on a data storage backend.
   *
   * @param data {@link ContentIterator} that should be stored.
   * @param transferSettings - settings how to deal with special cases as: - already existing data -
   */
  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {
    if (!(data instanceof IterativeData)) {
      throw new LcmExposableException("Unable to store streaming data directly to csv.");
    }
    ContentIterator content = ((IterativeData) data).getIterator();
    CsvAdapter csvAdapter = getCsvAdapter(key);

    try {
      if (csvAdapter.exists() && !transferSettings.isForceOverwrite()) {
        if (progressIndicationFactory != null) {
          String message =
              "The file: " + getFilePath(key) + " is already attached, won't overwrite.";
          progressIndicationFactory.writeIndication(message);
        }
        throw new LcmExposableException("Data set is already attached, won't overwrite. Data item: " + key);
      }

      int rowNumber = 1;

      try (OutputStream out = getCsvAdapter(key).getOutputStream()) {

        CsvConfiguration configuration = csvMetaData.getConfiguration(key);
        CsvWriter csvWriter = new CsvWriter(configuration);

        if (progressIndicationFactory != null) {
          String message = "Start transfer. File: " + getFilePath(key);
          progressIndicationFactory.writeIndication(message);
        }
        while (content.hasNext()) {

          Map row = content.next();

          if (rowNumber == configuration.getColumnNameLineNumber()) {
            Object[] lineAsObjectValues = (Object[]) row.keySet().toArray(new Object[] {});
            String[] lineAsStringValues = toStringArray(lineAsObjectValues);
            String columnLine = csvWriter.buildLine(lineAsStringValues);
            out.write(columnLine.getBytes());
            rowNumber++;
          }

          Object[] lineAsObjectValues = (Object[]) row.values().toArray(new Object[] {});
          String[] lineAsStringValues = toStringArray(lineAsObjectValues);
          String line = csvWriter.buildLine(lineAsStringValues);
          out.write(line.getBytes());
          rowNumber++;
          if (progressIndicationFactory != null
              && rowNumber % progressIndicationFactory.getIndicationChunkSize() == 0) {
            String message = "Written " + (rowNumber - 1) + " records!";
            progressIndicationFactory.writeIndication(message);
          }
        }
        out.flush();
        String message = "Written successfully all the records: " + (rowNumber - 1);
        if (progressIndicationFactory != null) {
          progressIndicationFactory.writeIndication(message);
        }
      } catch (IOException ex) {
        LOGGER.error("Error occured during saving information!", ex);
        if (progressIndicationFactory != null) {
          String message =
              String.format("The content is inserted partially, only %d rows in file: %s",
                  (rowNumber - 1), getFilePath(key));
          progressIndicationFactory.writeIndication(message);
        }
      }
    } catch (IOException ex) {
      LOGGER.error("Unable to write file: " + getFilePath(key) + ". Error message:"
          + ex.getMessage());
      if (progressIndicationFactory != null) {
        String message =
            "Transfer of the file: " + getFilePath(key) + " failed. Error message: "
                + ex.getMessage();
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
    try (InputStream stream = getCsvAdapter(key).getInputStream();) {

      UpdateableDataContext dataContext = createDataContext(stream, key);
      Schema schema = dataContext.getDefaultSchema();
      if (schema.getTableCount() == 0) {
        return null;
      }
      Table table = schema.getTables()[0];
      DataSet result = dataContext.query().from(table).selectAll().execute();
      csvMetaData.getTableDescription(key).setColumns(table.getColumns());

      return new IterativeData(new DataSetContentIterator(result));
    } catch (IOException ex) {
      LOGGER.error("Unable to read the file: " + getFilePath(key) + ". Error message: "
          + ex.getMessage());
      return null;
    }
  }

  @Override
  public boolean delete(String key) {
    CsvAdapter csvAdapter = getCsvAdapter(key);
    try {
      return csvAdapter.delete();
    } catch (Exception ex) {
      LOGGER.warn("Unable to delete file: " + getFilePath(key) + ". Error message: "
          + ex.getMessage());
    }
    return false;
  }

  @Override
  public void free() {

  }

  @Override
  protected List loadDataItems(String storageName, String subPath) {
    Storage storage = storageService.findByName(storageName);
    FileSystemAdapter fileSystem = getFileSystemAdapter(storage);
    try {
      return fileSystem.listFileNames(subPath);
    } catch (IOException ex) {
      LOGGER.error("Unable to laod data items");
      return null;
    }
  }

  private FileSystemAdapter getFileSystemAdapter(Storage storage) {
    FileSystemAdapter fileSystemAdapter = null;
    Notification notification = new Notification();
    if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileSystemAdapter = new AzureFileSystemAdapter(new AzureStorage(storage));
    } else if (LocalFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileSystemAdapter = new LocalFileSystemAdapter(new LocalFileStorage(storage));
    } else {
      LOGGER.warn("Improper storage object is passed to BackendCsvImpl. Storage id: "
          + storage.getId());
      notification.addError("Improper storage object is passed to BackendCsvImpl.");
      throw new LcmValidationException(notification);
    }


    return fileSystemAdapter;
  }


  private CsvAdapter getCsvAdapter(String key) {
    CsvAdapter csvAdapter = null;
    String storageName = getStorageName(key);
    Storage storage = storageService.findByName(storageName);
    if (storage == null) {
      LOGGER.error("Storage with name: " + storageName + " does not exists!");
      return null;
    }

    String filePath = getFilePath(key);

    if (LocalFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      csvAdapter = new LocalCsvAdapter(new LocalFileStorage(storage), filePath);
    } else if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      csvAdapter = new AzureCsvAdapter(new AzureStorage(storage), filePath);
    } else {
      LOGGER.warn("Improper storage object is passed to BackendCsvImpl. Storage id: "
          + storage.getId());
      Notification notification = new Notification();
      notification.addError("Improper storage object is passed to BackendCsvImpl.");
      throw new LcmValidationException(notification);
    }

    csvAdapter.validatePaths();
    return csvAdapter;
  }

  private UpdateableDataContext createDataContext(InputStream stream, String key) {
    if (metaDataWrapper == null) {
      throw new IllegalStateException("MetaData parameter could not be null");
    }

    CsvConfiguration csvConfiguration = null;
    if (csvMetaData.doesConfigurationExists(key)) {
      csvConfiguration = csvMetaData.getConfiguration(key);
    } else {
      csvConfiguration = csvMetaData.getDefaultConfiguration();
    }

    return (CsvDataContext) DataContextFactory.createCsvDataContext(stream, csvConfiguration);
  }
}
