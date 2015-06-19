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

package nl.kpmg.lcm.server.data;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import nl.kpmg.lcm.server.metadata.MetaData;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;


/**
 *
 * @author Pavel Jez
 */


public class BackendFileTest {
    
    private static final String TEST_STORAGE_PATH = "temp_test/";
    
    @Before
    public void setUp() throws Exception {
        // make test temp dir and set storage path
        File testDir = new File(TEST_STORAGE_PATH);
        testDir.mkdir();
    }
    
    @After
    public void tearDown() {
        File file = new File(TEST_STORAGE_PATH);
         for (File c : file.listFiles())
            c.delete();
        file.delete();
    }
    
    @Test
    public void testGetSupportedUriSchema(){
        File testDir = new File(TEST_STORAGE_PATH);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        String testSchema =  testBackend.getSupportedUriSchema();
        assertEquals("file",testSchema);
    }
    
    @Test
    public void testParseUri() throws BackendException,IOException {
        File testDir = new File(TEST_STORAGE_PATH);
        String uri = "file://"+testDir.getCanonicalPath()+"/temp.csv";
        BackendFileImpl testBackend = new BackendFileImpl(testDir); 
        URI dataUri = testBackend.parseUri(uri);
        String filePath = dataUri.getPath();
        assertEquals(testDir.getCanonicalPath()+"/temp.csv",filePath);
    }
    
    @Test
    public void testGatherDatasetInformationEmptyMetadata() throws BackendException {
        MetaData metaData = new MetaData();
        File testDir = new File(TEST_STORAGE_PATH);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
    }
    
    @Test(expected=BackendException.class)
    public void testGatherDatasetInformationWrongMetadata() throws BackendException {
        MetaData metaData = new MetaData();
        final String fileUri = "NotAnUri";
        metaData.put("data", new HashMap() {{ put("uri", fileUri); }});
        File testDir = new File(TEST_STORAGE_PATH);
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
    }
    
    @Test
    public void testGatherDatasetInformationWrongLink() throws BackendException,IOException {
        MetaData metaData = new MetaData();
        File testDir = new File(TEST_STORAGE_PATH);
        // need to make sure that test file does not exist
        File testFile = new File(TEST_STORAGE_PATH+"/temp.csv");
        testFile.delete(); 
        final String fileUri = "file://"+testDir.getCanonicalPath()+"/temp.csv";
        metaData.put("data", new HashMap() {{ put("uri", fileUri); }});
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(),false);
    }
    
    @Test
    public void testGatherDatasetInformation() throws BackendException,IOException {
        MetaData metaData = new MetaData();
        File testDir = new File(TEST_STORAGE_PATH);
        File testFile = new File(TEST_STORAGE_PATH+"/temp.csv");
        if (!testFile.exists())
            new FileOutputStream(testFile).close();
        Date exp_timestamp = new Date(testFile.lastModified());
        final String fileUri = "file://"+testDir.getCanonicalPath()+"/temp.csv";
        metaData.put("data", new HashMap() {{ put("uri", fileUri); }});
        BackendFileImpl testBackend = new BackendFileImpl(testDir);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.getModificationTime() ,exp_timestamp);
    }
    
     @Test
  public void evaluatesExpression() {
    int sum = 1+2+3;
    assertEquals(6, sum);
  }
}

