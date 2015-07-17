/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server.task.enrichment;

import java.io.File;
import java.util.HashMap;
import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.BackendDao;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.service.BackendService;
import nl.kpmg.lcm.server.task.TaskException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author mhoekstra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context-file.xml"})
public class DataEnrichmentTaskTest implements ApplicationContextAware {
    private static final String TEST_STORAGE_PATH = "test";

    private ApplicationContext context;

    @Autowired
    private MetaDataDao metaDataDao;

    @Autowired
    private BackendService backendService;

    @Autowired
    private BackendDao backendDao;

    @BeforeClass
    public static void setUpClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.mkdir();
    }

    @AfterClass
    public static void tearDownClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.delete();
    }

    @After
    public void tearDown() {
        File file;
        file = new File(TEST_STORAGE_PATH + "/metadata");
        for (File metaDataFolder : file.listFiles()) {
            for (File versionFile : metaDataFolder.listFiles()) {
                versionFile.delete();
            }
            metaDataFolder.delete();
        }
        file = new File(TEST_STORAGE_PATH + "/backend");
        for (File backendFile : file.listFiles()) {
            backendFile.delete();
        }
    }

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
        BackendModel backendModel = new BackendModel();
        backendModel.setName("test");
        backendModel.setOptions(new HashMap());
        backendModel.getOptions().put("storagePath", TEST_STORAGE_PATH + "/storage");
        backendDao.persist(backendModel);


        MetaData metaData = new MetaData();
        metaData.setDataUri("file://test/test");
        metaDataDao.persist(metaData);

        DataEnrichmentTask dataEnrichmentTask = new DataEnrichmentTask();
        autowire(dataEnrichmentTask);
        dataEnrichmentTask.execute(metaData);

        assertEquals("DETACHED", metaData.get("dynamic.data.state"));
    }
}
