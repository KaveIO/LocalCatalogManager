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
import java.sql.SQLException;
import java.sql.Statement;
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
    public void testGetSupportedUriSchema() {
        System.out.println("getSupportedUriSchema");
        String server = "jdbc:hive://localhost:10000/default";
        BackendHiveImpl instance = new BackendHiveImpl(server);
        String expResult = "jdbc:hive";
        String result = instance.getSupportedUriSchema();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testConnection() throws SQLException {
        System.out.println("testConnection");
        String server = "jdbc:hive2://localhost:10000/default";
        String user = "jpavel";
        String passwd = "";
        
        try {
            Class.forName(driverName);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Connection con = DriverManager.getConnection(server, user, passwd);
        Statement stmt = con.createStatement();
     //   con.close();
        
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
