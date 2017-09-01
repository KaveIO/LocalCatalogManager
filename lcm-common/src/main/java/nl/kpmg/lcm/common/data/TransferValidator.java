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
package nl.kpmg.lcm.common.data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class TransferValidator {
  private static List<String> structuredDataFormats;
  private static List<String> unstructuredDataFormats;

  static {
    structuredDataFormats =
        Arrays.asList(DataFormat.CSV, DataFormat.JSON, DataFormat.MONGO, DataFormat.HIVE, DataFormat.AZURECSV);
    unstructuredDataFormats =
        Arrays
            .asList(DataFormat.FILE, DataFormat.S3FILE, DataFormat.HDFSFILE, DataFormat.AZUREFILE);
  }

  public static boolean validateTransfer(String dataFormat, String storageFormat) {
    if (structuredDataFormats.contains(dataFormat) && structuredDataFormats.contains(storageFormat)) {
      return true;
    } else if (unstructuredDataFormats.contains(dataFormat)
        && unstructuredDataFormats.contains(storageFormat)) {
      return true;
    } else if (dataFormat.equals(DataFormat.JSON) && isUnstructuredDataFormat(storageFormat)) {
      return true;
    } else  if((dataFormat.equals(DataFormat.AZUREFILE) || dataFormat.equals(DataFormat.AZURECSV))
            && (storageFormat.equals(DataFormat.AZUREFILE) || storageFormat.equals(DataFormat.AZURECSV))) {
        return true;
    }
    return false;
  }

  public static List<String> getValidStorageTypes(String dataFormat) {
    List<String> result = null;
    if (structuredDataFormats.contains(dataFormat)) {
      result = new LinkedList<>();
      result.addAll(structuredDataFormats);
      // At this point when we want to transfer structured to unstructured data formats, the only
      // available combination is from JSON to unstructured data formats. This is because of the
      // fact that the incoming stream of structured data is always internally transformed to
      // JSON.
      if (dataFormat.equals(DataFormat.JSON)) {
        result.addAll(unstructuredDataFormats);
      }
    } else if (unstructuredDataFormats.contains(dataFormat)) {
      result = new LinkedList<>();
      result.addAll(unstructuredDataFormats);
    }
    return result;
  }

  public static boolean isStructuredDataFormat(String dataFormat) {
    if (structuredDataFormats.contains(dataFormat)) {
      return true;
    }
    return false;
  }

  public static boolean isUnstructuredDataFormat(String dataFormat) {
    if (unstructuredDataFormats.contains(dataFormat)) {
      return true;
    }
    return false;
  }
}
