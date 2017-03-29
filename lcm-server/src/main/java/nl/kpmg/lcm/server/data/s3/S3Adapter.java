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
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import nl.kpmg.lcm.server.backend.storage.S3FileStorage;

import java.io.InputStream;

/**
 *
 * @author shristov
 */
public class S3Adapter {

  private String lcmUniqueKey;
  private final String bucketName;

  private final AmazonS3 s3;

  public S3Adapter(S3FileStorage storage) {

    AWSCredentials credentials = new BasicAWSCredentials(storage.getAwsAccessKey(),
            storage.getAwsAccessKey());

    s3 = new AmazonS3Client(credentials);
    s3.setRegion(Region.getRegion(Regions.EU_WEST_1));
    bucketName = "lcm-" + lcmUniqueKey + "-" + storage.getName();
  }

  public void writeFile(String fileName, InputStream stream, int size) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(size);
    s3.putObject(new PutObjectRequest(bucketName, fileName, stream, metadata));
  }

  public InputStream readFile(String fileName) {
    S3Object object = s3.getObject(new GetObjectRequest(bucketName, fileName));
    return object.getObjectContent();
  }
}
