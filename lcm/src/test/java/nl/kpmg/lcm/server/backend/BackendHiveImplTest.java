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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.MetaData;
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
public class BackendHiveImplTest {
    
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    
    public BackendHiveImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     */
    @Test
    public void testGetSupportedUriSchema() throws BackendException {
        System.out.println("getSupportedUriSchema");
        String server = "hive://localhost:10000/default/table";
        BackendHiveImpl instance = new BackendHiveImpl(server);
        String expResult = "hive";
        String result = instance.getSupportedUriSchema();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testConnection() throws SQLException {
        System.out.println("testConnection");
        String server = "jdbc:hive2://localhost:10000/default";
        String user = "";
        String passwd = "";
        
        try {
            Class.forName(driverName);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Connection con = DriverManager.getConnection(server, user, passwd);
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery("describe formatted default.nyse_stocks");
        System.out.println(res.getMetaData().getColumnCount()+" columns");
        while (res.next()) {
         System.out.println(res.getString(1)+" "+res.getString(2)+" "+res.getString(3));
        }
        con.close();
        
    }
    
    @Test
    public final void testGatherDatasetInformation() throws BackendException {
        System.out.println("testGatherDatasetInformation");
        MetaData metaData = new MetaData();
        final String fileUri = "hive://jpavel@localhost:10000/default/batting";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHiveImpl testBackend = new BackendHiveImpl(fileUri);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(), true);
    }
    
      @Test
    public final void testGatherDatasetInformation2() throws BackendException {
        System.out.println("testGatherDatasetInformation");
        MetaData metaData = new MetaData();
        final String fileUri = "hive://jpavel@localhost:10000/default/battingNot";
        metaData.put("data", new HashMap() { { put("uri", fileUri); } });
        BackendHiveImpl testBackend = new BackendHiveImpl(fileUri);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        assertEquals(dataSetInformation.isAttached(), false);
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
