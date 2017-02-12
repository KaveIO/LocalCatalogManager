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

package nl.kpmg.lcm.server.task.enrichment;

import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.task.TaskException;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class DataFetchTaskTest {

    @Test(expected = TaskException.class)
    public void testValidationMissingOptions() throws TaskException{
        DataFetchTask task =  new DataFetchTask();
        Map options = null;
        MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
        task.execute(metaDataWrapper, options);
    }

    @Test(expected = TaskException.class)
    public void testValidationMissingPath() throws TaskException{
        DataFetchTask task =  new DataFetchTask();
        Map options = new HashMap();
        options.put("remoteLcm", "349bncdwqe8g89g74137823tr8");
        MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
        task.execute(metaDataWrapper, options);
    }

    @Test(expected = TaskException.class)
    public void testValidationMissingRemoteLcm() throws TaskException{
        DataFetchTask task =  new DataFetchTask();
        Map options = new HashMap();
        options.put("path", "/x/y/z");
        MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
        task.execute(metaDataWrapper, options);
    }
}
