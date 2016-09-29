/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.server.LcmBaseTest;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.task.TaskException;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;

/**
 *
 * @author mhoekstra
 */
public class DataEnrichmentTaskTest extends LcmBaseTest implements ApplicationContextAware {

  private ApplicationContext context;

  @Autowired
  private MetaDataDao metaDataDao;

  @Autowired
  private StorageService backendService;

  @Autowired
  private StorageDao backendDao;

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
  }

  private void autowire(DataEnrichmentTask task) {
    AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
    beanFactory.autowireBean(task);
  }

  @Test
  public void testExecuteWithExistingMetaData() throws TaskException {
    Storage backendModel = new Storage();
    backendModel.setId("test");
    backendModel.setOptions(new HashMap());
    backendModel.getOptions().put("storagePath", "test/storage");
    backendDao.save(backendModel);


    MetaData metaData = new MetaData();
    metaData.setDataUri("file://test/test");
    metaDataDao.save(metaData);

    DataEnrichmentTask dataEnrichmentTask = new DataEnrichmentTask();
    autowire(dataEnrichmentTask);
    dataEnrichmentTask.execute(metaData);

    assertEquals("DETACHED", metaData.get("dynamic.data.state"));
  }
}
