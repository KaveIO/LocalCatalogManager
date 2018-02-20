/*
 * Copyright 2017 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.DataState;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.StreamingData;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.backend.storage.HdfsFileStorage;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.data.FileAdapter;
import nl.kpmg.lcm.server.data.FileSystemAdapter;
import nl.kpmg.lcm.server.data.LocalFileAdapter;
import nl.kpmg.lcm.server.data.LocalFileSystemAdapter;
import nl.kpmg.lcm.server.data.azure.AzureFileAdapter;
import nl.kpmg.lcm.server.data.azure.AzureFileSystemAdapter;
import nl.kpmg.lcm.server.data.hdfs.HdfsFileAdapter;
import nl.kpmg.lcm.server.data.hdfs.HdfsFileSystemAdapter;
import nl.kpmg.lcm.server.data.s3.S3FileAdapter;
import nl.kpmg.lcm.server.data.s3.S3FileSystemAdapter;
import nl.kpmg.lcm.server.data.service.StorageService;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author shristov
 */
@BackendSource(type = {DataFormat.FILE, DataFormat.S3FILE, DataFormat.HDFSFILE,
    DataFormat.AZUREFILE})
public class BackendFileImpl extends AbstractBackend {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BackendFileImpl.class
      .getName());

  public BackendFileImpl(MetaData metaData, StorageService storageService) {
    super(metaData, storageService);

  }

  private FileAdapter getFileAdapter(String key) {
    FileAdapter fileAdapter = null;

    String storageName = getStorageName(key);
    Storage storage = storageService.findByName(storageName);
    if (storage == null) {
      LOGGER.error("Storage with name: " + storageName + " does not exists!");
      return null;
    }

    String filePath = getFilePath(key);

    if (S3FileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new S3FileAdapter(new S3FileStorage(storage), filePath);
    } else if (LocalFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new LocalFileAdapter(new LocalFileStorage(storage), filePath);
    } else if (HdfsFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new HdfsFileAdapter(new HdfsFileStorage(storage), filePath);
    } else if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new AzureFileAdapter(new AzureStorage(storage), filePath);
    } else {
      LOGGER.warn("Improper storage object is passed to BackendFileImpl. Storage id: "
          + storage.getId());
      Notification notification = new Notification();
      notification.addError("Improper storage object is passed to BackendFileImpl.");
      throw new LcmValidationException(notification);
    }

    fileAdapter.validatePaths();
    return fileAdapter;
  }

  @Override
  protected void enrichMetadataItem(EnrichmentProperties properties, String key) throws IOException {
    DataItemsDescriptor dynamicDataDescriptor =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
    FileAdapter fileAdapter = null;
    try {
      fileAdapter = getFileAdapter(key);
    } catch (LcmValidationException ex) {
      String state = DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
      LOGGER.warn("The metadata with id: " + metaDataWrapper.getId()
          + " has problems with storage validation. " + ex.getNotification().errorMessage());
      return;
    } catch (Exception ex) {
      String state = DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
      LOGGER
          .warn("Metadata id: " + metaDataWrapper.getId() + ". Error message: " + ex.getMessage());
      return;
    }

    try {
      if (!fileAdapter.exists()) {
        String state = DataState.DETACHED;
        dynamicDataDescriptor.getDetailsDescriptor().setState(state);
        LOGGER.warn("The metadata with id: " + metaDataWrapper.getId()
            + " has storage directory which does not exist.");
        return;
      }
    } catch (Exception ex) {
      String state = DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
      LOGGER
          .warn("Metadata id: " + metaDataWrapper.getId() + ". Error message: " + ex.getMessage());
      return;
    }

    if (properties.getAccessibility()) {
      String state = fileAdapter.exists() ? DataState.ATTACHED : DataState.DETACHED;
      dynamicDataDescriptor.getDetailsDescriptor().setState(state);
    }

    if (properties.getSize()) {
      dynamicDataDescriptor.getDetailsDescriptor().setSize(fileAdapter.length());
    }

    dynamicDataDescriptor.getDetailsDescriptor().setDataUpdateTimestamp(fileAdapter.lastModified());
  }

  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {

    if (!(data instanceof StreamingData)) {
      throw new LcmException("Unable to store iterative data directly to file.");
    }

    Long size =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .getSize();

    if (size == null) {
      LOGGER.warn("The file: " + getFilePath(key)
          + " can`t be transferred because it doesn`t exist.");
      String message =
          "The file: " + getFilePath(key) + " can`t be transferred because it doesn`t exist.";
      progressIndicationFactory.writeIndication(message);
      return;
    }

    FileAdapter fileAdapter = getFileAdapter(key);

    InputStream in = null;
    try {
      if (fileAdapter.exists() && !transferSettings.isForceOverwrite()) {
        if (progressIndicationFactory != null) {
          String message =
              "The file: " + getFilePath(key) + " is already attached, won't overwrite.";
          progressIndicationFactory.writeIndication(message);
        }
        throw new LcmException("Data set is already attached, won't overwrite. Data item: "
            + getFilePath(key));
      }

      if (progressIndicationFactory != null) {
        String message = "Start transfer. File: " + getFilePath(key);
        progressIndicationFactory.writeIndication(message);
      }

      StreamingData streamingData = (StreamingData) data;
      in = streamingData.getInputStream();

      fileAdapter.write(in, size);

      if (progressIndicationFactory != null) {
        String message = "The file: " + getFilePath(key) + " was transfered successfully.";
        progressIndicationFactory.writeIndication(message);
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
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ex) {
          // nothing to do
        }
      }
    }
  }


  @Override
  public Data read(String key) {
    FileAdapter fileAdapter = getFileAdapter(key);
    InputStream in;
    try {
      in = fileAdapter.read();
      if (in != null) {
        return new StreamingData(in);
      }
    } catch (IOException ex) {
      LOGGER.error("Unable to read file: " + getFilePath(key) + ". Error message:"
          + ex.getMessage());
    }

    return null;
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
    if (S3FileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileSystemAdapter = new S3FileSystemAdapter(new S3FileStorage(storage));
    } else if (LocalFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileSystemAdapter = new LocalFileSystemAdapter(new LocalFileStorage(storage));
    } else if (HdfsFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileSystemAdapter = new HdfsFileSystemAdapter(new HdfsFileStorage(storage));
    } else if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileSystemAdapter = new AzureFileSystemAdapter(new AzureStorage(storage));
    } else {
      LOGGER.warn("Improper storage object is passed to BackendFileImpl. Storage id: "
          + storage.getId());
      notification.addError("Improper storage object is passed to BackendFileImpl.");
      throw new LcmValidationException(notification);
    }


    return fileSystemAdapter;
  }

}