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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.BackendFactory;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmExposableException;
import nl.kpmg.lcm.server.test.mock.MetaDataMocker;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

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
    private BackendFactory backendFactory ;

    @Mock
    private BackendCsvImpl backendCsvImp;

    @InjectMocks
    private StorageService storageService;

    private Storage csvStorage;
    private String csvSchame = DataFormat.CSV;
    private String csvStorageName = "csv-storage";
    private String csvStorageURI = csvSchame + "://" + csvStorageName + "/test.csv";
    
    private MetaDataWrapper validMetaDataWrapper = MetaDataMocker.getCsvMetaDataWrapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        csvStorage = StorageMocker.createCsvStorage();
        List uriList = new ArrayList();
        uriList.add(csvStorageURI);
        validMetaDataWrapper.getData().setUri(uriList);
    }

    @Test
    public void testGetBackendCSV() throws Exception {
        given(storageDao.findOneByName(csvStorageName)).willReturn(csvStorage);        
        MetaDataWrapper metadataWapper = new MetaDataWrapper();
        List uriList = new ArrayList();
        uriList.add(csvStorageURI);
        metadataWapper.getData().setUri(uriList);
        given(backendFactory.createBackend(eq(csvSchame), eq(storageService), eq(metadataWapper))).willReturn(backendCsvImp);
        Backend result = storageService.getBackend(metadataWapper);

        assertNotNull(result);
    }

    @Test(expected = LcmExposableException.class)
    public void testGetBackendCSVInvalidURI() throws Exception {
        given(storageDao.findOneByName(csvStorageName)).willReturn(csvStorage);
        given(backendFactory.createBackend(csvSchame, storageService, validMetaDataWrapper)).willReturn(backendCsvImp);
        MetaDataWrapper metadataWapper = new MetaDataWrapper();
        List uriList = new ArrayList();
        uriList.add("invalid URI");
        metadataWapper.getData().setUri(uriList);
        storageService.getBackend(metadataWapper);
    }

    @Test(expected = LcmExposableException.class)
    public void testGetBackendCSVNullMetadata() throws Exception {
        given(storageDao.findOneByName(csvStorageName)).willReturn(csvStorage);
        given(backendFactory.createBackend(csvSchame, storageService, validMetaDataWrapper)).willReturn(backendCsvImp);
        storageService.getBackend(null);
    }

    @Test(expected = LcmExposableException.class)
    public void testGetBackendCSVMissingStorage() throws Exception {
        String nonExistingStorageName = "nonExistingStorageName";
        given(storageDao.findOneByName(nonExistingStorageName)).willReturn(null);
        MetaDataWrapper metadataWapper = new MetaDataWrapper();
        List uriList = new ArrayList();
        uriList.add("csv://" + nonExistingStorageName + "/test.csv");
        metadataWapper.getData().setUri(uriList);
        metadataWapper.getData().setUri(uriList);
        Backend result = storageService.getBackend(metadataWapper);

        assertNotNull(result);
    }

}
