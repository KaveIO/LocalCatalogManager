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
package nl.kpmg.lcm.server.data.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author shristov
 */
@Service
public class DataTransformationService {
  private final Set<String> tabularDataFormats = new HashSet();

  private final Set<String> fileDataFormats = new HashSet();

  private final Set<String> itemDataFormats = new HashSet();

  public DataTransformationService() {
    tabularDataFormats.add("csv");
    tabularDataFormats.add("hive");
    tabularDataFormats.add("sql");

    fileDataFormats.add("file");
    fileDataFormats.add("s3file");

    itemDataFormats.add("json");
    itemDataFormats.add("mongo");
  }

  public boolean isTransformationAdmissible(String inputDataFormat, String outputDataFormat) {
    if (tabularDataFormats.contains(inputDataFormat)
        && tabularDataFormats.contains(outputDataFormat)) {
      return true;
    }

    if (fileDataFormats.contains(inputDataFormat) && fileDataFormats.contains(outputDataFormat)) {
      return true;
    }

    if (itemDataFormats.contains(inputDataFormat) && itemDataFormats.contains(outputDataFormat)) {
      return true;
    }

    if (fileDataFormats.contains(outputDataFormat)) {
      return true;
    }

    return false;
  }
}
