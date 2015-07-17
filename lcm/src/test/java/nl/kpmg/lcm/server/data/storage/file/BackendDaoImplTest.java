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
package nl.kpmg.lcm.server.data.storage.file;

import nl.kpmg.lcm.server.data.dao.file.BackendDaoImpl;
import nl.kpmg.lcm.server.data.dao.DaoException;
import java.io.File;
import java.util.HashMap;
import nl.kpmg.lcm.server.data.BackendModel;
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
public class BackendDaoImplTest {
    private static final String TEST_STORAGE_PATH = "test/";

    private final BackendDaoImpl backendDao;

    public BackendDaoImplTest() throws DaoException {
        File file = new File(TEST_STORAGE_PATH);
        file.mkdir();

        backendDao = new BackendDaoImpl(TEST_STORAGE_PATH);
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
        for (File backendFolder : file.listFiles()) {
            backendFolder.delete();
        }
    }

    @Test
    public void testPersist()  {
        BackendModel backendmodel = new BackendModel();
        backendmodel.setName("test");
        backendmodel.setOptions(new HashMap());
        backendmodel.getOptions().put("storagePath", "/tmp/");

        backendDao.persist(backendmodel);

        BackendModel byName = backendDao.getByName("test");
        String path = (String) byName.getOptions().get("storagePath");
        assertEquals("/tmp/", path);

        assertEquals("test", byName.getName());
        assertEquals("/tmp/", (String) byName.getOptions().get("storagePath"));

    }
}
