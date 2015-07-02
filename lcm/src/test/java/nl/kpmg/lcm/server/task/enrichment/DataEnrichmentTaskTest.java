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
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.file.MetaDataDaoImpl;
import nl.kpmg.lcm.server.data.service.BackendService;
import nl.kpmg.lcm.server.task.TaskException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author mhoekstra
 */
public class DataEnrichmentTaskTest {
    private static final String TEST_STORAGE_PATH = "test/";

    private final MetaDataDaoImpl metaDataDao;


    private final BackendService backendService;

    public DataEnrichmentTaskTest() throws DaoException {
        File file = new File(TEST_STORAGE_PATH);
        file.mkdir();

        metaDataDao = new MetaDataDaoImpl(TEST_STORAGE_PATH);
                Resources.setMetaDataDao(metaDataDao);

        backendService = new BackendService();
        Resources.setBackendService(backendService);
    }

    @BeforeClass
    public static void setUpClass() {
        File file = new File(TEST_STORAGE_PATH);
        file.mkdir();
    }

    @AfterClass
    public static void tearDownClass() {
        File file = new File(TEST_STORAGE_PATH);
        file.delete();
    }

    @After
    public void tearDown() {
        File file = new File(TEST_STORAGE_PATH);
        for (File metaDataFolder : file.listFiles()) {
            for (File versionFile : metaDataFolder.listFiles()) {
                versionFile.delete();
            }
            metaDataFolder.delete();
        }
    }

    @Test
    public void testExecuteWithExistingMetaData() throws TaskException {
        MetaData metaData = new MetaData();
        metaData.setDataUri("file:///tmp/test");
        metaDataDao.persist(metaData);

        DataEnrichmentTask dataEnrichmentTask = new DataEnrichmentTask();
        dataEnrichmentTask.execute(metaData);

        assertEquals("DETACHED", metaData.get("dynamic.data.state"));
    }

}
