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

import nl.kpmg.lcm.server.LcmBaseTest;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mhoekstra
 */
public class DataEnrichmentTaskTest extends LcmBaseTest implements ApplicationContextAware {

  private ApplicationContext context;

  @Autowired
  private MetaDataDao metaDataDao;


  @Autowired
  private StorageDao storageDao;

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
  }

  private void autowire(DataEnrichmentTask task) {
    AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
    beanFactory.autowireBean(task);
  }

  //TODO  refactore this test case !!!
  @Ignore("Disable until csv backend is online")
  @Test
  public void testExecuteWithExistingMetaData() throws TaskException {
    Storage storage = StorageMocker.createCsvStorage();
    storageDao.save(storage);

    MetaDataWrapper metaDataWrapper = new MetaDataWrapper();
    List uriList = new ArrayList();
    uriList.add("file://test/test");
    metaDataWrapper.getData().setUri(uriList);
    metaDataDao.save(metaDataWrapper.getMetaData());

    DataEnrichmentTask dataEnrichmentTask = new DataEnrichmentTask();
    autowire(dataEnrichmentTask);
    TaskDescription td = new TaskDescription();
    dataEnrichmentTask.execute(metaDataWrapper, td.getOptions());

    //assertEquals("DETACHED", metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).get getState());
  }
}
