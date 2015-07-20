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
package nl.kpmg.lcm.server.backend;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.MetaData;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jpavel
 */
public class BackendHDFSImplTest {
    
    private static final String TEST_STORAGE_PATH = "temp_test/";
    
    public BackendHDFSImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        // make test temp dir and set storage path
        File testDir = new File(TEST_STORAGE_PATH);
        boolean mkdir = testDir.mkdir();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSupportedUriSchema method, of class BackendHDFSImpl.
     */
    @Test
    public final void testGetSupportedUriSchema() {
        System.out.println("getSupportedUriSchema");
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        String expResult = "hdfs";
        String result = testBackend.getSupportedUriSchema();
        assertEquals(expResult, result);
    }
    
    /**
     * Tests if the URI is parsed correctly in {@link BackendHDFSImpl} class.
     * 
     * @throws BackendException if it is not possible to parse the URI
     *
     */
    @Test
    public final void testParseUri() throws BackendException {
        System.out.println("parseUri");
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        String uri = server + "user/name/temp.csv";
        URI dataUri = testBackend.parseUri(uri);
        String filePath = dataUri.getPath();
        System.out.println(filePath);
        assertEquals("/user/name/temp.csv", filePath);
    }
    
     /**
     * Tests if the exception is thrown if wrong  URI scheme is parsed.
     * 
     * @throws BackendException if it works correctly.
     * 
     */
    @Test(expected = BackendException.class)
    public final void testParseFileUri() throws BackendException {
        System.out.println("parseFileUri");
        String server = "file:///";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        String uri = server + "user/name/temp.csv";
        URI dataUri = testBackend.parseUri(uri); 
        // If we got here, we failed to convince the parseUri to throw an exception
        fail("parseUri did not thrown BackendException!");
    }
    
    @Test
    public final void testAccess() throws IOException {
        URI uri = URI.create ("hdfs://localhost:9000/user/jpavel/file.txt");
        Configuration conf = new Configuration ();
        conf.set("fs.default.name", "hdfs://localhost:9000");
        FileSystem file = FileSystem.get(conf);
        System.out.println("get file"+file.exists(new Path(uri)));
    }
    
    /**
     * Tests what happens if {@link BackendFileImp} gathers information using empty
     * {@link MetaData} object. Exception is expected.
     *
     * @throws BackendException if empty metadata are supplied.
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationEmptyMetadata() throws BackendException {
       System.out.println("testGatherDatasetInformationEmptyMetadata");
        MetaData metaData = new MetaData();
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        fail("testGatherDatasetInformationEmptyMetadata did not thrown BackendException!");
    }
    
    @Test
    public final void testGatherDatasetInformationWrongMetadata() throws BackendException {
       System.out.println("testGatherDatasetInformationWrongMetadata");
        MetaData metaData = new MetaData();
        final String fileUri = "NotAnUri";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(), false);
    }
    
    /**
     * Tests what happens if {@link BackendFileImp} gathers information using
     * {@link MetaData} object with valid URI pointing to non-existing location.
     * The {@link DataSetInformation} object should has isAttached() method equal to false.
     * 
     * @throws BackendException if it is not possible to gather information about the
     *         dataset
     * @throws IOException if it is not possible to get path of the storage directory
     */
    @Test
    public final void testGatherDatasetInformationWrongLink() throws BackendException, IOException {
      System.out.println("testGatherDatasetInformationWrongLink");
        MetaData metaData = new MetaData();
//        File testDir = new File(TEST_STORAGE_PATH);
//        // need to make sure that test file does not exist
//        File testFile = new File(TEST_STORAGE_PATH + "/temp.csv");
//        testFile.delete();
        final String fileUri = "hdfs://localhost:9000/user/jpavel/NoFile";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(), false);
    }
    
    @Test
    public final void testGatherDatasetInformation() throws BackendException, IOException {
        System.out.println("testGatherDatasetInformation");
        MetaData metaData = new MetaData();
//        File testDir = new File(TEST_STORAGE_PATH);
//        // need to make sure that test file does not exist
//        File testFile = new File(TEST_STORAGE_PATH + "/temp.csv");
//        testFile.delete();
        final String fileUri = "hdfs://localhost:9000/user/jpavel/file3.txt";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        System.out.println(dataSetInformation.getModificationTime());
        System.out.println(dataSetInformation.isReadable());
        System.out.println(dataSetInformation.getByteSize());
        assertEquals(dataSetInformation.isAttached(), true);
    }
    
    @Test
    public final void testStore() throws IOException, BackendException {
        //first make a test file with some content
        File testFile = new File(TEST_STORAGE_PATH + "/testFile.csv");
        testFile.createNewFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            final int nLoops = 10;
            for (int i = 0; i  < nLoops; i++) {
                writer.write("qwertyuiop");
                writer.write("\n");
                writer.write("asdfghjkl");
                writer.write("\n");
                writer.write("zxcvbnm,!@#$%^&*()_");
                writer.write("\n");
                writer.write("1234567890[][;',.");
                writer.write("\n");
            }
            writer.flush();
        }
        // now make a metadata with uri
        File testDir = new File(TEST_STORAGE_PATH);
        final String fileUri = "hdfs://localhost:9000/user/jpavel/testStore.csv";
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        InputStream is = new FileInputStream(testFile);
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        testBackend.store(metaData, is);
//        final File expected = testFile;
//        
//        final File output = new File(testDir.getCanonicalPath() + "/testStore.csv");
//        HashCode hcExp = Files.hash(expected, Hashing.md5());
//        HashCode hcOut = Files.hash(output, Hashing.md5());
//        assertEquals(hcExp.toString(), hcOut.toString());
    }
    
    @Test
    public final void testRead() throws IOException, BackendException {
        // make a metadata with uri
        final String fileUri = "hdfs://localhost:9000/user/jpavel/testStore.csv";
        // make test file to where the content stored in previous test would be read
        File testFile = new File(TEST_STORAGE_PATH + "/testRead.csv");
        testFile.createNewFile();
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        // make local data backend in specified directory and read the existing file
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        try (InputStream is = testBackend.read(metaData)) {
            try (FileOutputStream fos = new FileOutputStream(testFile)) {
                int readBytes = IOUtils.copy(is, fos);
                Logger.getLogger(BackendFileImpl.class.getName())
                        .log(Level.INFO, "{0} bytes read", readBytes);
                fos.flush();
            }
        }
//        // check if the files are identical
//        final File expected = testFile;
//        final File output = new File(testDir.getCanonicalPath() + "/testStore.csv");
//        HashCode hcExp = Files.hash(expected, Hashing.md5());
//        HashCode hcOut = Files.hash(output, Hashing.md5());
//        assertEquals(hcExp.toString(), hcOut.toString());
    }
    
     @Test
    public final void testDelete() throws IOException, BackendException {
        final String fileUri = "hdfs://localhost:9000/user/jpavel/testStore.csv";
        // make metadata pointing to the file to be deleted
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        // make local data backend in specified directory and delete the existing file
        String server = "hdfs://localhost:9000/";
        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
        boolean result = testBackend.delete(metaData);
        assertEquals(result, true);
    }

//    /**
//     * Test of gatherDataSetInformation method, of class BackendHDFSImpl.
//     */
//    @Test
//    public void testGatherDataSetInformation() throws Exception {
//        System.out.println("gatherDataSetInformation");
//        MetaData metadata = null;
//        BackendHDFSImpl instance = new BackendHDFSImpl();
//        DataSetInformation expResult = null;
//        DataSetInformation result = instance.gatherDataSetInformation(metadata);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of store method, of class BackendHDFSImpl.
//     */
//    @Test
//    public void testStore() throws Exception {
//        System.out.println("store");
//        MetaData metadata = null;
//        InputStream content = null;
//        BackendHDFSImpl instance = new BackendHDFSImpl();
//        instance.store(metadata, content);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of read method, of class BackendHDFSImpl.
//     */
//    @Test
//    public void testRead() throws Exception {
//        System.out.println("read");
//        MetaData metadata = null;
//        BackendHDFSImpl instance = new BackendHDFSImpl();
//        InputStream expResult = null;
//        InputStream result = instance.read(metadata);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of delete method, of class BackendHDFSImpl.
//     */
//    @Test
//    public void testDelete() throws Exception {
//        System.out.println("delete");
//        MetaData metadata = null;
//        BackendHDFSImpl instance = new BackendHDFSImpl();
//        boolean expResult = false;
//        boolean result = instance.delete(metadata);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
