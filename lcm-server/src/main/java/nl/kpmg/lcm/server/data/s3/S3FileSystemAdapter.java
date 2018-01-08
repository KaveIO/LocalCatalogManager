package nl.kpmg.lcm.server.data.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.data.FileSystemAdapter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

/**
 *
 * @author shristov
 */
public class S3FileSystemAdapter implements FileSystemAdapter {
 //private final S3FileStorage storage;
private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(S3FileAdapter.class.getName());
  private String bucketName;

  private AmazonS3 s3Client;
  private String fileName;

  public S3FileSystemAdapter(S3FileStorage s3Storage) {
    String secretAcccessKey;
    secretAcccessKey = s3Storage.getAwsSecretAccessKey();

    AWSCredentials credentials =
        new BasicAWSCredentials(s3Storage.getAwsAccessKey(), secretAcccessKey);
    AWSStaticCredentialsProvider credentialsProvider =
        new AWSStaticCredentialsProvider(credentials);
    s3Client = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider)
        .withRegion(Regions.EU_WEST_1).build();

    bucketName = s3Storage.getBucketName();
  }

  @Override
  public List listFileNames(String subPath) throws IOException {

    if(subPath.charAt(subPath.length() -1) != '/') {
        subPath =  subPath + '/';
    }

    LinkedList<String> fileNameList = new LinkedList();
    ObjectListing listing;
    do {
      listing = s3Client.listObjects(bucketName, subPath);
      List<S3ObjectSummary> objectSummaryList = listing.getObjectSummaries();

      for (S3ObjectSummary objectSummary : objectSummaryList) {
        String key = objectSummary.getKey();
        int index = StringUtils.lastIndexOf(key, "/");
        String itemName = key.substring(index);
        fileNameList.add(itemName);
      }
    } while (listing.isTruncated());

    return fileNameList;
  }

    @Override
    public TestResult testConnection() throws IOException {

    boolean  result = s3Client.doesBucketExist(bucketName);
    if(result){
        return new TestResult("OK.", TestResult.TestCode.ACCESIBLE);
    }
    return new TestResult("Storage bucket does not exists or it is not accessible!",
        TestResult.TestCode.INACCESSIBLE);
    }

}
