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
package nl.kpmg.lcm.server.test.mock;

import nl.kpmg.lcm.server.data.meatadata.MetaDataWrapper;

import java.util.HashMap;

/**
 *
 * @author shristov
 */
public class MetaDataMocker {
    public static MetaDataWrapper getCsvMetaData(){
        MetaDataWrapper metaDataWrapper =  new MetaDataWrapper();
        metaDataWrapper.setName("CSV-metadata");
        metaDataWrapper.setId("585a57136d31212d0ad5fca6");
        metaDataWrapper.setSourceType("csv");
        metaDataWrapper.getData().setUri("csv://test/temp.csv");
        metaDataWrapper.getData().setOptions(new HashMap());
        metaDataWrapper.getGeneralInfo().setOwner("KPMG");
        metaDataWrapper.getGeneralInfo().setDescription("Sample description");

        return metaDataWrapper;
    }

     public static MetaDataWrapper getHiveMetaData(){
        MetaDataWrapper metaDataWrapper =  new MetaDataWrapper();
        metaDataWrapper.setName("Hive-metadata");
        metaDataWrapper.setId("585a57236d31212d0ad5fca6");
        metaDataWrapper.setSourceType("hive");
        metaDataWrapper.getData().setUri("hive://remote-hive-foodmart/product");
        metaDataWrapper.getData().setOptions(new HashMap());
        metaDataWrapper.getGeneralInfo().setOwner("KPMG");
        metaDataWrapper.getGeneralInfo().setDescription("Sample description");

        return metaDataWrapper;
    }
}
