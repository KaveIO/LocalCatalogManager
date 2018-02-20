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

package nl.kpmg.lcm.server.data.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.data.FileAdapter;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author shristov
 */
public class S3FileAdapter implements FileAdapter {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(S3FileAdapter.class
      .getName());
  private String bucketName;

  private AmazonS3 s3Client;
  private String fileName;

  private S3Object object;

  public S3FileAdapter(S3FileStorage s3Storage, String fileName) {
    String secretAcccessKey;
    secretAcccessKey = s3Storage.getAwsSecretAccessKey();

    AWSCredentials credentials =
        new BasicAWSCredentials(s3Storage.getAwsAccessKey(), secretAcccessKey);
    AWSStaticCredentialsProvider credentialsProvider =
        new AWSStaticCredentialsProvider(credentials);
    s3Client =
        AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider)
            .withRegion(Regions.EU_WEST_1).build();

    bucketName = s3Storage.getBucketName();
    if (fileName.charAt(0) == '/') { // amazon s3 service don't like "/" in front of the file name
      this.fileName = fileName.substring(1);
    } else {
      this.fileName = fileName;
    }
    object = s3Client.getObject(new GetObjectRequest(bucketName, fileName));
  }

  @Override
  public void write(InputStream stream, Long size) throws IOException {
    if (size == null || size <= 0) {
      throw new LcmException("Error! Unable to transfer file to s3 storage with unknown size.");
    }

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(size);
    if (!s3Client.doesBucketExist(bucketName)) {
      s3Client.createBucket(bucketName);
    }
    s3Client.putObject(new PutObjectRequest(bucketName, fileName, stream, metadata));
    LOGGER.info("Successfully written data in s3 storage. Bucket:  " + bucketName);
  }

  @Override
  public InputStream read() throws IOException {
    if (object != null) {
      return object.getObjectContent();
    }

    return null;
  }

  @Override
  public boolean exists() throws IOException {
    return s3Client.doesObjectExist(bucketName, fileName);
  }

  @Override
  public long length() throws IOException {
    return object.getObjectMetadata().getContentLength();
  }

  @Override
  public long lastModified() throws IOException {
    return object.getObjectMetadata().getLastModified().getTime();
  }

  @Override
  public void validatePaths() {
    if (fileName.contains("../")) {
      throw new LcmValidationException(
          "Metadata path is probbably wrong. Data uri can not contains \"..\\\"!");
    }
  }

  @Override
  public boolean delete() throws Exception {
    s3Client.deleteObject(new DeleteObjectRequest(bucketName, object.getKey()));
    return true;
  }
}
