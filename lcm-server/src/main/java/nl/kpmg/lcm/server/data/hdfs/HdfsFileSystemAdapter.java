package nl.kpmg.lcm.server.data.hdfs;

import nl.kpmg.lcm.server.backend.storage.HdfsFileStorage;
import nl.kpmg.lcm.server.data.FileSystemAdapter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

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
public class HdfsFileSystemAdapter implements FileSystemAdapter {
 private final HdfsFileStorage storage;

  public HdfsFileSystemAdapter(HdfsFileStorage storage) {
    this.storage = storage;
  }

    @Override
    public List listFileNames(String subPath) throws IOException {

    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", storage.getUrl());
    FileSystem hdfs = FileSystem.get(conf);
    String storagePath = "/" + storage.getPath() + "/" + subPath;
    Path filePath = new Path(storagePath);
    if (!hdfs.exists(filePath)) {
        return null;
    }

     RemoteIterator<LocatedFileStatus> fileList = hdfs.listFiles(filePath, false);
     LinkedList<String> fileNameList =  new LinkedList();

     while(fileList.hasNext()){
         LocatedFileStatus fileStatus =  fileList.next();
         fileNameList.add(fileStatus.getPath().getName());
     }

     return fileNameList;
    }
}
