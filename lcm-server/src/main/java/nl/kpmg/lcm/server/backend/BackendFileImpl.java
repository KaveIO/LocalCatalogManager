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
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.s3.S3Adapter;
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
@BackendSource(type = {DataFormat.FILE, DataFormat.S3FILE})
public class BackendFileImpl extends AbstractBackend {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BackendCsvImpl.class
      .getName());
  private FileAdapter fileAdapter;

  public BackendFileImpl(Storage storage, MetaData metaData) {
    super(metaData);

    String filePath = metaDataWrapper.getData().getStorageItemName();
    if (S3FileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new S3Adapter(new S3FileStorage(storage), filePath);
    } else if (LocalFileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      fileAdapter = new LocalFileAdapter(new LocalFileStorage(storage), filePath);
    } else {
      LOGGER.warn("Improper storage object is passed to BackendFileImpl. Storage id: "
          + storage.getId());
      Notification notification = new Notification();
      notification.addError("Improper storage object is passed to BackendFileImpl.");
      throw new LcmValidationException(notification);
    }
  }

  @Override
  public MetaData enrichMetadata(EnrichmentProperties properties) {
    long start = System.currentTimeMillis();

    String filePath = metaDataWrapper.getData().getStorageItemName();
    try {
      metaDataWrapper.clearDynamicData();
      if (properties.getAccessibility()) {
        String state = fileAdapter.exists() ? "ATTACHED" : "DETACHED";
        metaDataWrapper.getDynamicData().setState(state);
      }

      if (fileAdapter.exists()) {
        if (properties.getSize()) {
          metaDataWrapper.getDynamicData().setSize(fileAdapter.length());
        }

        metaDataWrapper.getDynamicData().setDataUpdateTimestamp(fileAdapter.lastModified());
      }
    } catch (Exception ex) {
      LOGGER.error("Unable to enrich medatadata : " + metaDataWrapper.getId() + ". Error Message: "
          + ex.getMessage());
      throw new LcmException("Unable to get info about datasource: " + filePath, ex);
    } finally {
      metaDataWrapper.getDynamicData().setUpdateTimestamp(new Date().getTime());
      long end = System.currentTimeMillis();
      metaDataWrapper.getDynamicData().setUpdateDurationTimestamp(end - start);
    }

    return metaDataWrapper.getMetaData();
  }

  @Override
  public void store(Data data, TransferSettings transferSettings) {

    if (!(data instanceof StreamingData)) {
      throw new LcmException("Unable to storeiterative data directly to file.");
    }

    Long size = metaDataWrapper.getDynamicData().getSize();

    String filePath = metaDataWrapper.getData().getStorageItemName();
    try {
      if (fileAdapter.exists() && !transferSettings.isForceOverwrite()) {
        throw new LcmException("Data set is already attached, won't overwrite.");
      }
      StreamingData streamingData = (StreamingData) data;
      InputStream in = streamingData.getInputStream();

      fileAdapter.write(in, size);
    } catch (IOException ex) {
      LOGGER.error("Unable to read file: " + filePath + ". Error message:" + ex.getMessage());
    }
  }

  @Override
  public Data read() {

    String filePath = metaDataWrapper.getData().getStorageItemName();
    InputStream in;
    try {
      in = fileAdapter.read();
      if (in != null) {
        return new StreamingData(metaDataWrapper.getMetaData(), in);
      }
    } catch (IOException ex) {
      LOGGER.error("Unable to read file: " + filePath + ". Error message:" + ex.getMessage());
    }

    return null;
  }

  @Override
  public boolean delete() {
    throw new UnsupportedOperationException("Backend delete operation is not supported yet.");
  }

  @Override
  public void free() {

  }
}