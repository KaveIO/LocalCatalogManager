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
package nl.kpmg.lcm.server.data.service;

import java.util.HashMap;
import java.util.Map;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.BackendFactory;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.server.data.service.exception.MissingStorageException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {StorageService.class})
public class StorageServiceTest {

    @Mock
    private StorageDao storageDao;

    @Mock
    private BackendFactory backendFactory;

    @Mock
    private BackendCsvImpl backendCsvImp;

    @InjectMocks
    private StorageService storageService;

    private Storage csvStorage;
    private String csvSchame = "csv";
    private String csvStoragePath = "/tmp";
    private String csvStorageName = "csv-storage";
    private String csvStorageURI = csvSchame + "://" + csvStorageName + "/test.csv";
    
    private MetaData validMetaData = new MetaData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        csvStorage = new Storage();
        csvStorage.setName(csvStorageName);
        Map options = new HashMap();
        options.put("storagePath", csvStoragePath);
        csvStorage.setOptions(options);
        
        validMetaData.setDataUri(csvStorageURI);
    }

    @Test
    public void testGetBackendCSV() throws Exception {
        given(storageDao.findOneByName(csvStorageName)).willReturn(csvStorage);        
        MetaData metadata = new MetaData();
        metadata.setDataUri(csvStorageURI);
        given(backendFactory.createBackend(eq(csvSchame), eq(csvStorage), eq(metadata))).willReturn(backendCsvImp);        
        Backend result = storageService.getBackend(metadata);

        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBackendCSVInvalidURI() throws Exception {
        given(storageDao.findOneByName(csvStorageName)).willReturn(csvStorage);
        given(backendFactory.createBackend(csvSchame, csvStorage, validMetaData)).willReturn(backendCsvImp);
        MetaData metadata = new MetaData();
        metadata.setDataUri("invalid URI");
        storageService.getBackend(metadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBackendCSVNullMetadata() throws Exception {
        given(storageDao.findOneByName(csvStorageName)).willReturn(csvStorage);
        given(backendFactory.createBackend(csvSchame, csvStorage, validMetaData)).willReturn(backendCsvImp);
        storageService.getBackend(null);
    }

    @Test(expected = MissingStorageException.class)
    public void testGetBackendCSVMissingStorage() throws Exception {
        String nonExistingStorageName = "nonExistingStorageName";
        given(storageDao.findOneByName(nonExistingStorageName)).willReturn(null);
        MetaData metadata = new MetaData();
        metadata.setDataUri("csv://" + nonExistingStorageName + "/test.csv");
        Backend result = storageService.getBackend(metadata);

        assertNotNull(result);
    }

}
