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
package nl.kpmg.lcm.server.data.hdfs;

import nl.kpmg.lcm.server.backend.storage.HdfsFileStorage;
import nl.kpmg.lcm.server.data.FileAdapter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author shristov
 */
public class HdfsAdapter implements FileAdapter {
  private String storagePath;
  private String storageUrl;
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HdfsAdapter.class
      .getName());

  public HdfsAdapter(HdfsFileStorage hdfsStorage, String fileName) {
    storagePath = "/" + hdfsStorage.getPath() + "/" + fileName;
    storageUrl = hdfsStorage.getUrl();
  }

  @Override
  public void write(InputStream stream, Long size) throws IOException {
    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", storageUrl);
    FileSystem hdfs = FileSystem.get(conf);
    Path file = new Path(storagePath);
    if (hdfs.exists(file)) {
      hdfs.delete(file, true);
    }
    OutputStream os = hdfs.create(file);
    IOUtils.copyBytes(stream, os, conf);
  }

  @Override
  public InputStream read() throws IOException {
    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", storageUrl);
    FileSystem hdfs = FileSystem.get(conf);
    Path file = new Path(storagePath);
    return hdfs.open(file);
  }

  @Override
  public boolean exists() throws IOException {
    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", storageUrl);
    FileSystem hdfs = FileSystem.get(conf);
    Path file = new Path(storagePath);
    return hdfs.exists(file);
  }

  @Override
  public long length() throws IOException {
    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", storageUrl);
    FileSystem hdfs = FileSystem.get(conf);
    Path file = new Path(storagePath);
    return hdfs.getFileStatus(file).getLen();
  }

  @Override
  public long lastModified() throws IOException {
    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", storageUrl);
    FileSystem hdfs = FileSystem.get(conf);
    Path file = new Path(storagePath);
    return hdfs.getFileStatus(file).getModificationTime();
  }

}
