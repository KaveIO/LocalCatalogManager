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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.MetaData;
import org.apache.hive.service.cli.HiveSQLException;
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
public class BackendHiveImplIntTest {

    /**
     * Class with the hive2 driver that is dynamically loaded.
     */
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    /**
     * Address of the hive2 server.
     */
    private final String TEST_STORAGE_PATH = "hive://192.168.56.101:10000";
    /**
     * Address of HDFS nameserver.
     */
    private final String HDFS_SERVER = "hdfs://192.168.56.101:8020";
    /**
     * Temporary directory in which all the test files will exist.
     */
    private static final String TEST_DIR = "temp_test/";

    
    /**
     * Common access tool for all backends.
     */
    private final BackendModel backendModel;

    /**
     * Default constructor.
     */
    public BackendHiveImplIntTest() {
        backendModel = new BackendModel();
        backendModel.setName("test");
        backendModel.setOptions(new HashMap());
        backendModel.getOptions().put("storagePath", TEST_STORAGE_PATH);
        backendModel.getOptions().put("hdfsServer", HDFS_SERVER);
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        // make test temp dir and set storage path
        File testDir = new File(TEST_DIR);
        boolean mkdir = testDir.mkdir();
        // make test csv file
        File testFile = new File(TEST_DIR + "/testFile.csv");
        testFile.createNewFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Var1,Var2,Var3,Var4");
            writer.write("\n");
            writer.write("Val1,Val2,Val3,Val4");
            writer.write("\n");
            writer.write("Val5,Val6,Val7,Val8");
            writer.write("\n");
            writer.write("Val9,Val10,Val11,Val12");
            writer.write("\n");
            writer.flush();
        }
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
     * Test of getSupportedUriSchema method, of class BackendHiveImpl. 
     * Method returns supported uri schema ("hive") and tests fails if it is 
     * not the case.
     * 
     */
    @Test
    public void testGetSupportedUriSchema() {
        System.out.println("getSupportedUriSchema");
        BackendHiveImpl instance = new BackendHiveImpl(backendModel);
        String expResult = "hive";
        String result = instance.getSupportedUriSchema();
        assertEquals(expResult, result);
    }
    /**
     * Test of connection to the hive server.
     * Method connects to the hive2 server specified in "storagePath" of backendModel.
     * It assumes existence of a database called "default" and lists all tables in this
     * database.
     *
     * @throws SQLException if there is a problem with connection or sql query
     * @throws BackendException if it is not possible to retrieve hive server address from the 
     * {@link BackendModel} instance that initialized the {@link BackendHiveImp} instance.
     */
    @Test
    public final void testConnection() throws SQLException, BackendException {
        System.out.println("testConnection will print all tables in \'default\' db");
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        URI hiveUri;
        hiveUri = testBackend.parseUri((String) backendModel.getOptions().get("storagePath"));
        String hostName = hiveUri.getHost();
        String port = Integer.toString(hiveUri.getPort());
        String server = testBackend.getURIscheme() + hostName + ":" + port + "/default";
        String user = "";
        String passwd = "";
        
        try {
            Class.forName(driverName);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplIntTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Connection con = DriverManager.getConnection(server, user, passwd);
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery("show tables in default");
       while (res.next()) {
                String resString = res.getString(1);
                System.out.println(resString);
            }
        con.close();
        
    }
    /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * {@link MetaData} object with valid URI pointing to existing location. The
     * {@link DataSetInformation} object should has isAttached() method equal to
     * true.
     *
     * @throws BackendException if it is not possible to gather information
     * about the dataset
     */
    @Test
    public final void testGatherDatasetInformation() throws BackendException {
        System.out.println("testGatherDatasetInformation");
        MetaData metaData = new MetaData();
        final String fileUri = TEST_STORAGE_PATH+"/default/batting";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        System.out.println("modification time: " + dataSetInformation.getModificationTime());
        System.out.println("is readable: " + dataSetInformation.isReadable());
        System.out.println("byte size is: " + dataSetInformation.getByteSize());
        assertEquals(dataSetInformation.isAttached(), true);
    }
    
    /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * empty {@link MetaData} object. Exception is expected.
     *
     * @throws BackendException if empty metadata are supplied.
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationEmptyMetadata() throws BackendException {
        System.out.println("testGatherDatasetInformationEmptyMetadata");
        MetaData metaData = new MetaData();
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        fail("testGatherDatasetInformationEmptyMetadata did not thrown BackendException!");
    }
//    
    /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * {@link MetaData} object with invalid URI. The method should fail as well 
     * because invalid uri scheme is supplied.
     *
     * @throws BackendException if invalid URI is supplied.
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationWrongMetadata() throws BackendException {
        System.out.println("testGatherDatasetInformationWrongMetadata");
        MetaData metaData = new MetaData();
        final String fileUri = "NotAnUri";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        fail("testGatherDatasetInformationWrongMetadata did not thrown BackendException!");
    }
    
     /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * {@link MetaData} object with valid but incomplete URI. The method should fail as well 
     * because the URI is not complete
     *
     * @throws BackendException if incomplete URI is supplied.
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationWrongLink() throws BackendException {
        System.out.println("testGatherDatasetInformationWrongLink");
        MetaData metaData = new MetaData();
        final String fileUri = "hive://NoPath";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
       fail("testGatherDatasetInformationWrongLink did not thrown BackendException!");
    }
    
     /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * {@link MetaData} object with valid URI pointing to non-existing location. The
     * {@link DataSetInformation} object should has isAttached() method equal to
     * false.
     *
     * @throws BackendException if it is not possible to gather information
     * about the dataset
     */
    @Test
    public final void testGatherDatasetInformationWrongDest() throws BackendException {
        System.out.println("testGatherDatasetInformationWrongDest");
        MetaData metaData = new MetaData();
        final String fileUri = TEST_STORAGE_PATH+"/default/batling";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);   
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        System.out.println("modification time: " + dataSetInformation.getModificationTime());
        System.out.println("is readable: " + dataSetInformation.isReadable());
        System.out.println("byte size is: " + dataSetInformation.getByteSize());
        assertEquals(dataSetInformation.isAttached(), false);
    }

    @Test
    public final void testStore() throws IOException, BackendException {
        

        // now make a metadata with uri
        final String fileUri = TEST_STORAGE_PATH + "/default/test_table4";
        MetaData metaData = new MetaData();
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        // connect to the csv file 
        File testFile = new File(TEST_DIR + "/testFile.csv");
        InputStream is = new FileInputStream(testFile);
        
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);   
        testBackend.store(metaData, is);
//        // copy the file back to check that it is ok
//        Process p;
//        try {
//            p = Runtime.getRuntime().exec("hdfs dfs -copyToLocal /user/test/testStore.csv "
//                    + TEST_DIR + "/testStore.csv");
//            p.waitFor();
//        } catch (IOException | InterruptedException ex) {
//            Logger.getLogger(BackendHDFSImpl.class.getName()).log(Level.SEVERE, "Cannot access the hdfs at "
//                    + TEST_STORAGE_PATH, ex);
//        }
//        // check if the files are identical
//        final File expected = testFile;
//        final File output = new File(TEST_DIR + "/testStore.csv");
//        HashCode hcExp = Files.hash(expected, Hashing.md5());
//        HashCode hcOut = Files.hash(output, Hashing.md5());
//        assertEquals(hcExp.toString(), hcOut.toString());
  }
    

    /**
     * Test of gatherDataSetInformation method, of class BackendHiveImpl.
     */
//    @Test
//    public void testGatherDataSetInformation() throws Exception {
//        System.out.println("gatherDataSetInformation");
//        MetaData metadata = null;
//        BackendHiveImpl instance = new BackendHiveImpl();
//        DataSetInformation expResult = null;
//        DataSetInformation result = instance.gatherDataSetInformation(metadata);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of store method, of class BackendHiveImpl.
//     */
//    @Test
//    public void testStore() throws Exception {
//        System.out.println("store");
//        MetaData metadata = null;
//        InputStream content = null;
//        BackendHiveImpl instance = new BackendHiveImpl();
//        instance.store(metadata, content);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of read method, of class BackendHiveImpl.
//     */
//    @Test
//    public void testRead() throws Exception {
//        System.out.println("read");
//        MetaData metadata = null;
//        BackendHiveImpl instance = new BackendHiveImpl();
//        InputStream expResult = null;
//        InputStream result = instance.read(metadata);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of delete method, of class BackendHiveImpl.
//     */
//    @Test
//    public void testDelete() throws Exception {
//        System.out.println("delete");
//        MetaData metadata = null;
//        BackendHiveImpl instance = new BackendHiveImpl();
//        boolean expResult = false;
//        boolean result = instance.delete(metadata);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
