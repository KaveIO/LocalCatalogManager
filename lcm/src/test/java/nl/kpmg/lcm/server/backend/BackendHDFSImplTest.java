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
import nl.kpmg.lcm.server.data.BackendModel;
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
 * Test suite for local hadoop installation. It is running on port 9000. 
 * @see <a href=
 * "http://hadoop.apache.org/docs/r2.7.0/hadoop-project-dist/hadoop-common/SingleCluster.html">Documentation</a>
 * 
 * 
 * It is ignored by default and run only during integration.
 * @author jpavel
 */
public class BackendHDFSImplTest {
    
    /**
     * Temporary directory in which all the test files will exist.
     */
    private static final String TEST_STORAGE_PATH = "hdfs://localhost:9000/";
    private static final String TEST_DIR = "temp_test/";

    /**
     * Common access tool for all backends.
     */
    private final BackendModel backendModel;

    /**
     * Default constructor.
     */
    public BackendHDFSImplTest() {
        backendModel = new BackendModel();
        backendModel.setName("test");
        backendModel.setOptions(new HashMap());
        backendModel.getOptions().put("storagePath", TEST_STORAGE_PATH);
    }
    
    /**
     * Makes a temporary test directory.
     * @return true if creation successful, false otherwise.
     * @throws Exception if it is not possible to make a test directory.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        // make test temp dir and set storage path
        File testDir = new File(TEST_DIR);
        boolean mkdir = testDir.mkdir();
        if (mkdir) {
            System.out.println("Setup BackendFileTest successful");
        } else {
            System.out.println("Setup BackendFileTest failed");
        }
    }
    
    /**
     * Deletes the temporary test directory and its content, assuming there are
     *  no subdirectories.
     */
    @AfterClass
    public static final void tearDownClass() {
        File file = new File(TEST_DIR);
         for (File c : file.listFiles()) {
            c.delete();
         }
        file.delete();
    }
    

    /**
     * Test of getSupportedUriSchema method, of class BackendHDFSImpl.
     */
    @Test
    public final void testGetSupportedUriSchema() {
        System.out.println("getSupportedUriSchema");
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
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
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
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
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
        String uri = server + "user/name/temp.csv";
        URI dataUri = testBackend.parseUri(uri); 
        // If we got here, we failed to convince the parseUri to throw an exception
        fail("parseUri did not thrown BackendException!");
    }
    
    @Test
    public final void testAccess() throws IOException {
        System.out.println("testAcess");
        URI uri = URI.create ("hdfs://localhost:9000/user/jpavel/file3.txt");
        Configuration conf = new Configuration ();
        String server = (String) backendModel.getOptions().get("storagePath");
        conf.set("fs.default.name", server);
        FileSystem file = FileSystem.get(conf);
        System.out.println("get file "+file.exists(new Path(uri)));
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
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        fail("testGatherDatasetInformationEmptyMetadata did not thrown BackendException!");
    }
    
    /**
     * Tests what happens if {@link BackendFileImp} gathers information using
     * {@link MetaData} object with invalid URI. 
     *
     * @throws BackendException if empty metadata are supplied.
     */
    @Test
    public final void testGatherDatasetInformationWrongMetadata() throws BackendException {
       System.out.println("testGatherDatasetInformationWrongMetadata");
        MetaData metaData = new MetaData();
        final String fileUri = "NotAnUri";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
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
        final String fileUri = TEST_STORAGE_PATH+"NoFile";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(), false);
    }
    
    /**
     * Tests what happens if {@link BackendFileImp} gathers information using
     * {@link MetaData} object with valid URI pointing to existing location.
     * The {@link DataSetInformation} object should has isAttached() method equal to true.
     * 
     * @throws BackendException if it is not possible to gather information about the
     *         dataset
     * @throws IOException if it is not possible to get path of the storage directory
     */
    @Test
    public final void testGatherDatasetInformation() throws BackendException, IOException {
        System.out.println("testGatherDatasetInformation");
        MetaData metaData = new MetaData();
        final String fileUri = "hdfs://localhost:9000/user/jpavel/file3.txt";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        System.out.println(dataSetInformation.getModificationTime());
        System.out.println(dataSetInformation.isReadable());
        System.out.println(dataSetInformation.getByteSize());
        assertEquals(dataSetInformation.isAttached(), true);
    }
    
    @Test
    public final void testStore() throws IOException, BackendException {
        //first make a test file with some content
        File testFile = new File(TEST_DIR + "/testFile.csv");
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
        final String fileUri = "hdfs://localhost:9000/user/jpavel/testStore.csv";
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        InputStream is = new FileInputStream(testFile);
        BackendHDFSImpl testBackend = new BackendHDFSImpl(backendModel);
        testBackend.store(metaData, is);
    }
//    
//    @Test
//    public final void testRead() throws IOException, BackendException {
//        // make a metadata with uri
//        final String fileUri = "hdfs://localhost:9000/user/jpavel/testStore.csv";
//        // make test file to where the content stored in previous test would be read
//        File testFile = new File(TEST_STORAGE_PATH + "/testRead.csv");
//        testFile.createNewFile();
//        MetaData metaData = new MetaData();
//        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
//        // make local data backend in specified directory and read the existing file
//        String server = "hdfs://localhost:9000/";
//        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
//        try (InputStream is = testBackend.read(metaData)) {
//            try (FileOutputStream fos = new FileOutputStream(testFile)) {
//                int readBytes = IOUtils.copy(is, fos);
//                Logger.getLogger(BackendFileImpl.class.getName())
//                        .log(Level.INFO, "{0} bytes read", readBytes);
//                fos.flush();
//            }
//        }
////        // check if the files are identical
////        final File expected = testFile;
////        final File output = new File(testDir.getCanonicalPath() + "/testStore.csv");
////        HashCode hcExp = Files.hash(expected, Hashing.md5());
////        HashCode hcOut = Files.hash(output, Hashing.md5());
////        assertEquals(hcExp.toString(), hcOut.toString());
//    }
//    
//     @Test
//    public final void testDelete() throws IOException, BackendException {
//        final String fileUri = "hdfs://localhost:9000/user/jpavel/testStore.csv";
//        // make metadata pointing to the file to be deleted
//        MetaData metaData = new MetaData();
//        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
//        // make local data backend in specified directory and delete the existing file
//        String server = "hdfs://localhost:9000/";
//        BackendHDFSImpl testBackend = new BackendHDFSImpl(server);
//        boolean result = testBackend.delete(metaData);
//        assertEquals(result, true);
//    }

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
