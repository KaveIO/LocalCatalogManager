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

import nl.kpmg.lcm.server.backend.storage.HdfsFileStorage;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.server.data.FileAdapter;
import nl.kpmg.lcm.server.data.LocalFileAdapter;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.StreamingData;
import nl.kpmg.lcm.server.data.TransferSettings;
import nl.kpmg.lcm.server.data.hdfs.HdfsAdapter;
import nl.kpmg.lcm.server.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.s3.S3Adapter;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * 
 * @author shristov
 */
@BackendSource(type = {DataFormat.FILE, DataFormat.S3FILE, DataFormat.HDFSFILE})
public class BackendFileImpl extends AbstractBackend {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BackendFileImpl.class
      .getName());

  public BackendFileImpl(MetaData metaData, StorageService storageService) {
    super(metaData, storageService);


  }

  private FileAdapter getFileAdapter(Storage storage, String filePath) {
    FileAdapter fileAdapter = null;
    Notification notification = new Notification();
    if (S3FileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new S3Adapter(new S3FileStorage(storage), filePath);
    } else if (LocalFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new LocalFileAdapter(new LocalFileStorage(storage), filePath);
    } else if (HdfsFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new HdfsAdapter(new HdfsFileStorage(storage), filePath);
    } else {
      LOGGER.warn("Improper storage object is passed to BackendFileImpl. Storage id: "
          + storage.getId());
      notification.addError("Improper storage object is passed to BackendFileImpl.");
      throw new LcmValidationException(notification);
    }


    return fileAdapter;
  }

  @Override
  public MetaData enrichMetadata(EnrichmentProperties properties) {
    expandDataURISection();
    if (metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors() == null) {
      return metaDataWrapper.getMetaData();
    }
    for (String key : metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors().keySet()) {
      long start = System.currentTimeMillis();
      DataItemsDescriptor dynamicDataDescriptor =
          metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
      String dataURI = dynamicDataDescriptor.getURI();
      String filePath = storageService.getStorageItemName(dataURI);
      Storage storage = storageService.getStorageByUri(dataURI);
      FileAdapter fileAdapter = getFileAdapter(storage, filePath);
      try {
        dynamicDataDescriptor.clearDetailsDescriptor();
        if (properties.getAccessibility()) {
          String state = fileAdapter.exists() ? "ATTACHED" : "DETACHED";
          dynamicDataDescriptor.getDetailsDescriptor().setState(state);
        }

        if (fileAdapter.exists()) {
          if (properties.getSize()) {
            dynamicDataDescriptor.getDetailsDescriptor().setSize(fileAdapter.length());
          }

          dynamicDataDescriptor.getDetailsDescriptor().setDataUpdateTimestamp(
              fileAdapter.lastModified());
        }
      } catch (Exception ex) {
        LOGGER.error("Unable to enrich medatadata : " + metaDataWrapper.getId()
            + ". Error Message: " + ex.getMessage());
        throw new LcmException("Unable to get info about datasource: " + filePath, ex);
      } finally {
        dynamicDataDescriptor.getDetailsDescriptor().setUpdateTimestamp(new Date().getTime());
        long end = System.currentTimeMillis();
        dynamicDataDescriptor.getDetailsDescriptor().setUpdateDurationTimestamp(end - start);
      }
    }
    return metaDataWrapper.getMetaData();

  }

  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {

    if (!(data instanceof StreamingData)) {
      throw new LcmException("Unable to storeiterative data directly to file.");
    }

    Long size =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .getSize();
    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    String filePath = storageService.getStorageItemName(dataURI);
   Storage storage = storageService.getStorageByUri(dataURI);
    FileAdapter fileAdapter = getFileAdapter(storage, filePath);

    try {
      if (fileAdapter.exists() && !transferSettings.isForceOverwrite()) {
        throw new LcmException("Data set is already attached, won't overwrite. Data item: " + dataURI);
      }
      StreamingData streamingData = (StreamingData) data;
      InputStream in = streamingData.getInputStream();

      fileAdapter.write(in, size);
    } catch (IOException ex) {
      LOGGER.error("Unable to read file: " + filePath + ". Error message:" + ex.getMessage());
    }
  }


  @Override
  public Data read(String key) {
    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    String filePath = storageService.getStorageItemName(dataURI);
    Storage storage = storageService.getStorageByUri(dataURI);
    FileAdapter fileAdapter = getFileAdapter(storage, filePath);
    InputStream in;
    try {
      in = fileAdapter.read();
      if (in != null) {
        return new StreamingData(in);
      }
    } catch (IOException ex) {
      LOGGER.error("Unable to read file: " + filePath + ". Error message:" + ex.getMessage());
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
}
