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
package nl.kpmg.lcm.server.metadata.storage.file;

import nl.kpmg.lcm.server.metadata.storage.StorageException;
import java.io.File;
import nl.kpmg.lcm.server.metadata.MetaData;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mhoekstra
 */
public class MetaDataDaoImplTest {
    private static final String TEST_STORAGE_PATH = "test/";

    private final MetaDataDaoImpl metaDataDao;

    public MetaDataDaoImplTest() throws StorageException {
        File file = new File(TEST_STORAGE_PATH);
        file.mkdir();

        metaDataDao = new MetaDataDaoImpl(TEST_STORAGE_PATH);
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
    public void testPersist() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);
    }

    @Test
    public void testPersistVersionZero() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);

        MetaData versionZeroExists = metaDataDao.getByNameAndVersion("test", "0");
        assertNotNull(versionZeroExists);

        MetaData versionOneMissing = metaDataDao.getByNameAndVersion("test", "1");
        assertNull(versionOneMissing);
    }

    @Test
    public void testGetByNameReturnsLatestVersion() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);
        MetaData metadata = metaDataDao.getByName("test");
        assertEquals("0", metadata.getVersionNumber());

        metaDataDao.persist(metaData);
        metadata = metaDataDao.getByName("test");
        assertEquals("1", metadata.getVersionNumber());
    }

    @Test
    public void testGetByNameAndVersionReturnsSpecificVersion() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);
        metaDataDao.persist(metaData);

        MetaData metadata = metaDataDao.getByNameAndVersion("test", "0");
        assertEquals("0", metadata.getVersionNumber());

        metadata = metaDataDao.getByNameAndVersion("test", "1");
        assertEquals("1", metadata.getVersionNumber());
    }

    @Test
    public void testDeleteRemovesMetaData() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);

        MetaData metadata = metaDataDao.getByName("test");
        assertNotNull(metadata);

        metaDataDao.delete(metaData);

        metadata = metaDataDao.getByName("test");
        assertNull(metadata);
    }
}
