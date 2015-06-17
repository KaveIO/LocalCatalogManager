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
import java.io.IOException;
import java.net.URI;
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
  public void evaluatesExpression() {
    int sum = 1+2+3;
    assertEquals(6, sum);
  }
}

