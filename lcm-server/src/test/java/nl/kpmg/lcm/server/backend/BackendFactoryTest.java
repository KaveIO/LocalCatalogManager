/*
  * Copyright 2016 KPMG N.V. (unless otherwise stated).
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  *  in compliance with the License. You may obtain a copy of the License at
  * 
  *  http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License
  *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  *  or implied. See the License for the specific language governing permissions and limitations under
  *  the License.
 */
package nl.kpmg.lcm.server.backend;

import static org.junit.Assert.*;

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.exception.LcmException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BackendFactory.class})
public class BackendFactoryTest {

    private Storage csvStorage;
    private String csvSchame = "csv";
    private String csvStoragePath = "/tmp";
    private String csvStorageName = "csv-storage";
    private String csvStorageURI = csvSchame + "://" + csvStorageName + "/test.csv";
    private MetaData validMetaData = new MetaData();
    

    @Autowired
    private BackendFactory backendFactory;

    public BackendFactoryTest() {
        csvStorage = new Storage();
        csvStorage.setName(csvStorageName);
        Map options = new HashMap();
        options.put("storagePath", csvStoragePath);
        csvStorage.setOptions(options);
        
        validMetaData.setDataUri(csvStorageURI);
    }

    @Test
    public void testCreateBackendCSV() throws Exception {
        Backend csvBackend = backendFactory.createBackend(csvSchame, csvStorage, validMetaData);
        assertNotNull(csvBackend);
        assertTrue(csvBackend instanceof BackendCsvImpl);
    }

    @Test(expected = LcmException.class)
    public void testCreateBackendInvalidSchema() throws Exception {
        backendFactory.createBackend("this_is_invalid_schema", csvStorage, validMetaData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBackendNullSchema() throws Exception {
        backendFactory.createBackend(null, csvStorage, validMetaData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBackendTooShortSchema() throws Exception {
        backendFactory.createBackend("", csvStorage, validMetaData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBackendTooLongSchema() throws Exception {
        String veryLongSchema = "111111111111111111111111111111111111111111"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1111111111111111111dfsgsdffffff";

        backendFactory.createBackend(veryLongSchema, csvStorage, validMetaData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBackendNullStorage() throws Exception {
        backendFactory.createBackend(csvSchame, null, validMetaData);
    }
}
