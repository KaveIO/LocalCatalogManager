/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.validation.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class S3FileStorage extends AbstractStorageContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3FileStorage.class.getName());


  public S3FileStorage(Storage storage) {
    super(storage);
  }

  public String getAwsAccessKey() {
    return (String) storage.getCredentials().get("aws-access-key");
  }

  public void setAwsAccessKey(String awsAcessKey) {
    storage.getCredentials().put("aws-access-key", awsAcessKey);
  }

  public String getAwsSecretAccessKey() {
    return (String) storage.getCredentials().get("aws-secret-access-key");
  }


  public String getBucketName() {
    return (String) storage.getOptions().get("bucket");
  }

  /**
   * Bucket name must be unique in the amazon S3 domain space so try to keep format like bellow:
   * "kpmg-lcm-" + system unique key + storage name;. Where "system unique key" is data specific to
   * creating system
   */
  public String setBucketName(String bucketName) {
    return (String) storage.getOptions().put("bucket", bucketName);
  }

  public String getFileType() {
    return storage.getType();
  }

  public String getName() {
    return storage.getName();
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    if (!getSupportedStorageTypes().contains(storage.getType())) {
      notification.addError("Storage validation: storage type does not match!");
    }

    if (getAwsAccessKey() == null || getAwsAccessKey().isEmpty()) {
      notification.addError("Access key is missing or is empty!", null);
    }

    validateCredentials(notification);
  }

  public void validateCredentials(Notification notification) {

    if (storage.getCredentials() == null) {
      notification.addError("Credentials does not exists!");
      return;
    }

    if (getAwsSecretAccessKey() == null) {
      notification.addError("Storage validation: \"aws-secret-access-key\" is missing!");
    }

    if (getBucketName() == null || getBucketName().isEmpty()) {
      notification.addError("Bucket name is missing or is empty!");
    }
  }

  /**
   *
   * @return a set with supported storages. i.e if the type of the pure storage object is csv then
   *         it could not be passed to Hive Wrapper(HiveStorage)
   */
  public static Set<String> getSupportedStorageTypes() {
    Set result = new HashSet();
    result.add(DataFormat.S3FILE);
    return result;
  }

  /**
   * @return a set with credentials fields that needs to be encrypted for this storage type
   */
  public static Set<String> getEncryptedCredentialsFields() {
    Set result = new HashSet();
    result.add("aws-secret-access-key");
    return result;
  }
}