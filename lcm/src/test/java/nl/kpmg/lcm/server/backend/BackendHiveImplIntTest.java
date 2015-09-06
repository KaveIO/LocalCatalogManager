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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.MetaData;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author jpavel
 */
public class BackendHiveImplIntTest {

    /**
     * Class with the hive2 driver that is dynamically loaded.
     */
    private static final String DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";
    /**
     * Address of the hive2 server.
     */
    private static final String TEST_STORAGE_PATH = "hive://192.168.56.101:10000";
    /**
     * Address of HDFS nameserver.
     */
    private static final String HDFS_SERVER = "hdfs://192.168.56.101:8020";
    /**
     * Privileged hive user name.
     */
    private static final String HIVE_USER = "hive";
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
        backendModel.getOptions().put("hiveUser", HIVE_USER);
    }
    /**
     * Method to make a test directory, test file and test tables in hive.
     *
     * @throws IOException if there is a problem during writing test file
     * @throws SQLException if there is a problem connecting to hive and/or executing queries
     * @throws BackendException if it is not possible to parse uri of the hive server
     */
    @BeforeClass
    public static void setUpClass() throws IOException, SQLException, BackendException {
        // make test temp dir and set storage path
        File testDir = new File(TEST_DIR);
        boolean mkdir = testDir.mkdir();
        // make test csv file
        File testFile = new File(TEST_DIR + "/testFile.csv");
        testFile.createNewFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("var1,var2,var3,var4");
            writer.write("\n");
            writer.write("Val1,Val2,Val3,Val4");
            writer.write("\n");
            writer.write("Val5,Val6,Val7,Val8");
            writer.write("\n");
            writer.write("Val9,Val10,Val11,Val12");
            writer.write("\n");
            writer.flush();
        }
        // make test table
        BackendModel testBackendModel = new BackendModel();
        testBackendModel.setName("test");
        testBackendModel.setOptions(new HashMap());
        testBackendModel.getOptions().put("storagePath", TEST_STORAGE_PATH);
        testBackendModel.getOptions().put("hdfsServer", HDFS_SERVER);
        testBackendModel.getOptions().put("hiveUser", HIVE_USER);

        BackendHiveImpl testBackend = new BackendHiveImpl(testBackendModel);
        URI hiveUri;
        hiveUri = testBackend.parseUri((String) testBackendModel.getOptions().get("storagePath"));
        String hostName = hiveUri.getHost();
        String port = Integer.toString(hiveUri.getPort());
        String server = testBackend.getURIscheme() + hostName + ":" + port + "/default";
        String user = HIVE_USER;
        String passwd = "";

        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplIntTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection con = DriverManager.getConnection(server, user, passwd)) {
            Statement stmt = con.createStatement();
            String sql = "CREATE TABLE default.lcm_test ";
            sql += "(var1 STRING,var2 STRING,var3 STRING,var4 STRING) ROW FORMAT DELIMITED ";
            sql += "FIELDS TERMINATED BY \',\' LINES TERMINATED by \'\\n\' STORED AS TEXTFILE";
            //  sql += " tblproperties (\"skip.header.line.count\"=\"1\")";
            stmt.execute(sql);
            sql = "INSERT INTO TABLE default.lcm_test VALUES "
                    + "(\"Val1\",\"Val2\",\"Val3\",\"Val4\"), "
                    + "(\"Val5\",\"Val6\",\"Val7\",\"Val8\"), "
                    + "(\"Val9\",\"Val10\",\"Val11\",\"Val12\")";
            stmt.execute(sql);
            // prepare table for test of deletion
            sql = "CREATE TABLE default.lcm_test_delete ";
            sql += "(var1 STRING,var2 STRING,var3 STRING,var4 STRING) ROW FORMAT DELIMITED ";
            sql += "FIELDS TERMINATED BY \',\' LINES TERMINATED by \'\\n\' STORED AS TEXTFILE";
            // sql += " tblproperties (\"skip.header.line.count\"=\"1\")";
            stmt.execute(sql);
            sql = "INSERT INTO TABLE default.lcm_test_delete VALUES"
                    + "(\"Wrong1\",\"Val2\",\"Val3\",\"Val4\"), "
                    + "(\"Val5\",\"Val6\",\"Val7\",\"Val8\"), "
                    + "(\"Val9\",\"Val10\",\"Val11\",\"Val12\")";
            stmt.execute(sql);
        }

    }
    /**
     * Clean-up class. Deletes test directory and test tables in hive
     *
     * @throws SQLException if there is a problem connecting to hive and/or executing queries
     * @throws BackendException if it is not possible to parse uri of the hive server
     */
    @AfterClass
    public static void tearDownClass() throws SQLException, BackendException {
        // delete the local directory + contents
        File file = new File(TEST_DIR);
        for (File c : file.listFiles()) {
            c.delete();
        }
        file.delete();
        // delete the test table
        BackendModel testBackendModel = new BackendModel();
        testBackendModel.setName("test");
        testBackendModel.setOptions(new HashMap());
        testBackendModel.getOptions().put("storagePath", TEST_STORAGE_PATH);
        testBackendModel.getOptions().put("hdfsServer", HDFS_SERVER);
        testBackendModel.getOptions().put("hiveUser", HIVE_USER);

        BackendHiveImpl testBackend = new BackendHiveImpl(testBackendModel);
        URI hiveUri;
        hiveUri = testBackend.parseUri((String) testBackendModel.getOptions().get("storagePath"));
        String hostName = hiveUri.getHost();
        String port = Integer.toString(hiveUri.getPort());
        String server = testBackend.getURIscheme() + hostName + ":" + port + "/default";
        String user = HIVE_USER;
        String passwd = "";

        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplIntTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Connection con = DriverManager.getConnection(server, user, passwd);
        Statement stmt = con.createStatement();
        String sql = "DROP TABLE default.lcm_test";
        stmt.execute(sql);
        sql = "DROP TABLE default.lcm_test_delete";
        stmt.execute(sql);
        sql = "DROP TABLE default.test_tableStore";
        stmt.execute(sql);
    }


    /**
     * Test of getSupportedUriSchema method, of class BackendHiveImpl. Method
     * returns supported uri schema ("hive") and tests fails if it is not the
     * case.
     *
     */
    @Test
    public final void testGetSupportedUriSchema() {
        System.out.println("getSupportedUriSchema");
        BackendHiveImpl instance = new BackendHiveImpl(backendModel);
        String expResult = "hive";
        String result = instance.getSupportedUriSchema();
        assertEquals(expResult, result);
    }

    /**
     * Test of connection to the hive server. Method connects to the hive2
     * server specified in "storagePath" of backendModel. It assumes existence
     * of a database called "default" and lists all tables in this database.
     *
     * @throws SQLException if there is a problem with connection or sql query
     * @throws BackendException if it is not possible to retrieve hive server
     * address from the {@link BackendModel} instance that initialized the
     * {@link BackendHiveImp} instance.
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
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplIntTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection con = DriverManager.getConnection(server, user, passwd)) {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("show tables in default");
            while (res.next()) {
                String resString = res.getString(1);
                System.out.println(resString);
            }
        }

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
        final String fileUri = TEST_STORAGE_PATH + "/default/lcm_test";
        metaData.setDataUri(fileUri);
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
        metaData.setDataUri(fileUri);
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        fail("testGatherDatasetInformationWrongMetadata did not thrown BackendException!");
    }

    /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * {@link MetaData} object with valid but incomplete URI. The method should
     * fail as well because the URI is not complete
     *
     * @throws BackendException if incomplete URI is supplied.
     */
    @Test(expected = BackendException.class)
    public final void testGatherDatasetInformationWrongLink() throws BackendException {
        System.out.println("testGatherDatasetInformationWrongLink");
        MetaData metaData = new MetaData();
        final String fileUri = "hive://NoPath";
        metaData.setDataUri(fileUri);
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        fail("testGatherDatasetInformationWrongLink did not thrown BackendException!");
    }

    /**
     * Tests what happens if {@link BackendHiveImp} gathers information using
     * {@link MetaData} object with valid URI pointing to non-existing location.
     * The {@link DataSetInformation} object should has isAttached() method
     * equal to false.
     *
     * @throws BackendException if it is not possible to gather information
     * about the dataset
     */
    @Test
    public final void testGatherDatasetInformationWrongDest() throws BackendException {
        System.out.println("testGatherDatasetInformationWrongDest");
        MetaData metaData = new MetaData();
        final String fileUri = TEST_STORAGE_PATH + "/default/lcm_trest";
        metaData.setDataUri(fileUri);
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
        System.out.println("modification time: " + dataSetInformation.getModificationTime());
        System.out.println("is readable: " + dataSetInformation.isReadable());
        System.out.println("byte size is: " + dataSetInformation.getByteSize());
        assertEquals(dataSetInformation.isAttached(), false);
    }

    /**
     * Tests store() method of {@link BackendHiveImp}. Test tries to store text
     * file created during setup as a table in hive, then it queries its content
     * and saves it to local text file and finally tests if the new text file is
     * identical to the original using md5.
     *
     * @throws BackendException if there is a problem in storing in hive
     * @throws IOException if it is not possible to read from or write to the
     * local file
     * @throws SQLException if there is a problem with querying the test table.
     */
    @Test
    public final void testStore() throws IOException, BackendException, SQLException {
        System.out.println("testStore");
        // now make a metadata with uri
        final String fileUri = TEST_STORAGE_PATH + "/default/test_tableStore";
        MetaData metaData = new MetaData();
        metaData.setDataUri(fileUri);
        // connect to the csv file
        File testFile = new File(TEST_DIR + "/testFile.csv");
        InputStream is = new FileInputStream(testFile);

        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        testBackend.store(metaData, is);

        // get the content of the test_tableStore table and store it in output file
        URI hiveUri;
        hiveUri = testBackend.parseUri((String) backendModel.getOptions().get("storagePath"));
        String hostName = hiveUri.getHost();
        String port = Integer.toString(hiveUri.getPort());
        String server = testBackend.getURIscheme() + hostName + ":" + port + "/default";
        String user = "";
        String passwd = "";

        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImplIntTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Connection con = DriverManager.getConnection(server, user, passwd);
        Statement stmt = con.createStatement();

        // to prevent printing table name in column headers
        stmt.execute("set hive.resultset.use.unique.column.names=false");
        ResultSet res = stmt.executeQuery("select * from default.test_tableStore");
        ResultSetMetaData rsmd = res.getMetaData();
        int numCol = rsmd.getColumnCount();
        File output = new File(TEST_DIR + "/testStore.csv");
        output.createNewFile();
        try (FileWriter writer = new FileWriter(output)) {
            String header = "";
            for (int iString = 1; iString < numCol; iString++) {
                header += rsmd.getColumnName(iString) + ",";
            }
            header += rsmd.getColumnName(numCol) + "\n";
            writer.write(header);
            while (res.next()) {
                String row = "";
                for (int iString = 1; iString < numCol; iString++) {
                    row += res.getString(iString) + ",";
                }
                row += res.getString(numCol) + "\n";
                writer.write(row);
            }
            writer.flush();
        }

        // check if the files are identical
        final File expected = testFile;
        final File obtained = output;
        HashCode hcExp = Files.hash(expected, Hashing.md5());
        HashCode hcOut = Files.hash(obtained, Hashing.md5());
        assertEquals(hcExp.toString(), hcOut.toString());
    }

    /**
     * Tests read() method of {@link BackendHDFSImp}. Test reads a text file
     * created by setup and stores in the new text file. Then it tests if the 2
     * files are identical using md5.
     *
     * @throws IOException if there are problems with creating local file or
     * writing to it
     * @throws BackendException if it is not possible to read from the test
     * backend
     */
    @Test
    public final void testRead() throws IOException, BackendException {
        // make a metadata with uri
        final String fileUri = TEST_STORAGE_PATH + "/default/lcm_test";
        // make test file to where the content stored in setup would be read
        File output = new File(TEST_DIR + "/testRead.csv");
        output.createNewFile();
        MetaData metaData = new MetaData();
        metaData.setDataUri(fileUri);
        // make local data backend in specified directory and read the existing file
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        try (InputStream is = testBackend.read(metaData)) {
            try (FileOutputStream fos = new FileOutputStream(output)) {
                int readBytes = IOUtils.copy(is, fos);
                Logger.getLogger(BackendHDFSImpl.class.getName())
                        .log(Level.INFO, "{0} bytes read", readBytes);
                fos.flush();
            }
        }

        Reader fr = new FileReader(output);
        BufferedReader br = new BufferedReader(fr);
        File output2 = new File(TEST_DIR + "/testRead_2.csv");
        output2.createNewFile();
        try (FileWriter writer = new FileWriter(output2)) {
            while (br.ready()) {
                String line = br.readLine();
                String newline = line.replaceAll("\u0001", ",");
                writer.write(newline + "\n");
            }
            writer.flush();
        }

        // check if the files are identical
        File testFile = new File(TEST_DIR + "/testFile.csv");
        final File expected = testFile;
        HashCode hcExp = Files.hash(expected, Hashing.md5());
        HashCode hcOut = Files.hash(output2, Hashing.md5());
        assertEquals(hcExp.toString(), hcOut.toString());
    }

    /**
     * Tests delete() method of {@link BackendHiveImp}. It tries to delete table
     * created in setup. It fails if it is not possible to.
     *
     * @throws BackendException if there is a problem during deletion
     */
    @Test
    public final void testDelete() throws BackendException {
        // make a metadata with uri
        final String fileUri = TEST_STORAGE_PATH + "/default/lcm_test_delete";

        MetaData metaData = new MetaData();
        metaData.setDataUri(fileUri);
        // make local data backend in specified directory and read the existing file
        BackendHiveImpl testBackend = new BackendHiveImpl(backendModel);
        boolean result = testBackend.delete(metaData);
        assertEquals(result, true);
    }

}
