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

package nl.kpmg.lcm.server.cron.job.processor;

import nl.kpmg.lcm.server.cron.job.processor.DataFetchExecutor;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class DataFetchTaskTest {

    @Test(expected = CronJobExecutionException.class)
    public void testValidationMissingOptions() throws CronJobExecutionException{
        DataFetchExecutor task =  new DataFetchExecutor();
        Map options = null;
        MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
        task.execute(metaDataWrapper, options);
    }

    @Test(expected = CronJobExecutionException.class)
    public void testValidationMissingPath() throws CronJobExecutionException{
        DataFetchExecutor task =  new DataFetchExecutor();
        Map options = new HashMap();
        options.put("remoteLcm", "349bncdwqe8g89g74137823tr8");
        MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
        task.execute(metaDataWrapper, options);
    }

    @Test(expected = CronJobExecutionException.class)
    public void testValidationMissingRemoteLcm() throws CronJobExecutionException{
        DataFetchExecutor task =  new DataFetchExecutor();
        Map options = new HashMap();
        options.put("path", "/x/y/z");
        MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
        task.execute(metaDataWrapper, options);
    }
}
