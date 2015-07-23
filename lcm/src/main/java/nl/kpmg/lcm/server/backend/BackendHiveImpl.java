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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.MetaData;

/**
 * Backend for Hive 2 server.
 *
 * @author jpavel
 */
public class BackendHiveImpl extends AbstractBackend {

    /**
     *
     * @param server is the server address.
     */
    private final String server;

    /**
     * @param DRIVER_NAME is the java hive driver class that is dynamically
     * loaded
     */
    private static final String DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";

    /**
     * @param URI_SCHEME is the URI scheme required by JDBC Hive2 client
     */
    private static final String URI_SCHEME = "jdbc:hive2://";
    /**
     * @param HDFS_PORT is the port on which the HDFS file system is accessible
     */
    private static final String HDFS_PORT = "8020";

    /**
     * Default constructor.
     *
     * @param backend is {@link BackendModel} that contains the storagePath,
     * i.e. server address (e.g. 127.0.0.1)
     */
    public BackendHiveImpl(final BackendModel backend) {
        this.server = (String) backend.getOptions().get("storagePath");

    }

    /**
     * Returns scheme supported by URI for this backend.
     *
     * @return "hive" string
     */
    @Override
    protected final String getSupportedUriSchema() {
        return "hive";
    }

    /**
     * Creates the full path for the connection from the specified URI.
     *
     * @param uri string in the supported format that is transformed to
     * connection string
     *
     * @return the connection string that is used to access the hive2 server
     * @throws BackendException when the URI is not in a correct format
     */
    private String makeConnectionString(final String uri) throws BackendException {
        String[] uriInfo = this.getServerDbTableFromUri(uri);
        String uriPort = this.getPortFromUri(uri);
        String out = "jdbc:hive2://" + uriInfo[0] + ":" + uriPort + "/" + uriInfo[1];
        return out;
    }

    /**
     * Gathers the host name, Database name and table name from the URI.
     *
     * @param uri string with the path to the server, database and the table
     * @return String array. First position is the server address, second is
     * database name and the last is the table name
     * @throws BackendException when URI is in wrong format and/or does not
     * contain the requested information
     */
    private String[] getServerDbTableFromUri(final String uri) throws BackendException {
        final int numOuts = 3;
        String[] out = new String[numOuts];
        if (uri != null) {
            URI dataUri;
            dataUri = parseUri(uri);
            out[0] = dataUri.getHost();
            String path = dataUri.getPath().substring(1); // remove the leading "/"
            String[] pathParts = path.split("/", 0);
            if (pathParts.length != 2) {
                throw new BackendException("Wrong URI format. Please use hive://[user]@host:port/DBname/TableName");
            }
            out[1] = pathParts[0];
            out[2] = pathParts[1];
        } else {
            throw new BackendException("Empty URI!");
        }
        return out;
    }

    /**
     * Gets port number from the URI.
     *
     * @param uri string with the path to the server and a port number
     * @return string that contains port number
     * @throws BackendException when URI is in wrong format and/or does not
     * contain the requested information
     */
    private String getPortFromUri(final String uri) throws BackendException {
        String out = "";
        if (uri != null) {
            URI dataUri;
            dataUri = parseUri(uri);
            int port = dataUri.getPort();
            out = Integer.toString(port);
        } else {
            throw new BackendException("Empty URI!");
        }
        return out;
    }

    /**
     * Gets user name from the URI.
     *
     * @param uri string with the path to the server and a user name
     * @return string with the user name
     * @throws BackendException when URI is in wrong format and/or does not
     * contain the requested information
     */
    private String getUserFromUri(final String uri) throws BackendException {
        String out = "";
        if (uri != null) {
            URI dataUri;
            dataUri = parseUri(uri);
            out = dataUri.getUserInfo();
        } else {
            throw new BackendException("Empty URI!");
        }
        return out;
    }

    /**
     * Helper function to extract necessary info from the hive query output.
     *
     * @param res Hive query output of the type {@link ResultSet}
     * @param dataSetInformation Information about the dataset
     * @return Updated {@link DatasetInformation}
     * @throws SQLException if there is problem with connection or querry
     * execution
     */
    private DataSetInformation
            getDBInfoFromResults(final ResultSet res, final DataSetInformation dataSetInformation) throws SQLException {
        final String protectString = "Protect Mode:";
        String protectResult = "";
        final String noProtection = "None";
        final String t1name = "Table Parameters:";
        boolean hasStatParams = false;
        long byteSize = -1;
        final int lastCol = 3;
        final String modString = "transient_lastDdlTime";
        String modDateString = "0";
        while (res.next() && !hasStatParams) {
            String resString = res.getString(1).trim();
            if (resString == null) {
                //method above can return null string, which causes switch to fail
                continue;
            }
            // trim necessary as function returns strings with trailing spaces
            String trimRes = resString.trim();
            switch (trimRes) {
                case t1name:
                    // detailed info saved, getting the size
                    hasStatParams = true;
                    final String paramName = "rawDataSize";
                    boolean hasSize = false;
                    boolean hasDate = false;
                    while (res.next() && (!hasSize || !hasDate)) {
                        String resPar = res.getString(2);
                        if (resPar == null) {
                            //method above can return null string, which causes switch to fail
                            continue;
                        }
                        String trim = resPar.trim();
                        switch (trim) {
                            case paramName:
                                byteSize = Long.parseLong(res.getString(lastCol).trim());
                                hasSize = true;
                                break;
                            case modString:
                                modDateString = res.getString(lastCol).trim();
                                hasDate = true;
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case protectString:
                    protectResult = res.getString(2).trim();
                    break;
                default:
                    break;
            }
        }
        dataSetInformation.setReadable(protectResult.equals(noProtection));
        dataSetInformation.setByteSize(byteSize);

        /**
         * @TODO missing protection in case the date is not found
         */
        long epochSecs = Long.parseLong(modDateString);
        final int numms = 1000;
        Date modDate = new Date(epochSecs * numms); // constructor expects ms
        dataSetInformation.setModificationTime(modDate);
        return dataSetInformation;
    }

    /**
     * Returns information about dataset mentioned in the metadata. It checks if
     * the referenced data exist and can be accessed. It also gathers
     * information about the size and modification time.
     *
     * @param metadata is investigated {@link MetaData} object
     * @return filled {@link DataSetInformation} object
     * @throws BackendException when there is a problem with the URI
     */
    @Override
    public final DataSetInformation gatherDataSetInformation(final MetaData metadata) throws BackendException {
        String uri = metadata.getDataUri();
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setUri(uri);

        String conPath = this.makeConnectionString(uri);
        String user = this.getUserFromUri(uri);
        String passwd = "";
        String[] uriInfo = this.getServerDbTableFromUri(uri);
        String tabName = uriInfo[2];
        String dbName = uriInfo[1];

        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection con = DriverManager.getConnection(conPath, user, passwd)) {
            Statement stmt = con.createStatement();
            // checking if table exists
            String sql = "show tables in " + dbName;
            ResultSet res = stmt.executeQuery(sql);
            boolean isAttached = false;
            while (res.next() && !isAttached) {
                String resString = res.getString(1);
                if (resString.equals(tabName)) {
                    isAttached = true;
                }
            }
            dataSetInformation.setAttached(isAttached);
            // getting further information about the dataset
            sql = "describe formatted " + dbName + "." + tabName;
            res = stmt.executeQuery(sql);
            dataSetInformation = this.getDBInfoFromResults(res, dataSetInformation);
            if (dataSetInformation.getByteSize() == -1) {
                // detailed information about size, etc. not available
                String sql2 = "analyze table " + dbName + "." + tabName + " compute statistics";
                stmt.execute(sql2);
                res = stmt.executeQuery(sql);
                dataSetInformation = this.getDBInfoFromResults(res, dataSetInformation);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        // analyze table default.test_yahoo2 compute statistics;
        // describe formatted test_yahoo2
        return dataSetInformation;
    }

    /**
     * Function to store an input stream as a table in Hive.
     *
     * The input stream must have csv format, i.e. table, columns separated by
     * commas and every entry on a new line. The function first create a file in
     * HDFS system in the home directory and gives it the same name as the table
     * specified by uri using {@link BackendHDFSImp} methods. It also reads the
     * first line of the provided input stream and uses it as a header.
     *
     * Then it creates a table using the header, assuming csv format. All
     * columns (variables) are assumed to be of a STRING type. Finally, it loads
     * the HDFS file into hive. This operation should delete the temporary file
     * in HDFS created by this function
     *
     * @param metadata {@link MetaData} object containing the URI of destination
     * table
     * @param content {@link InputStream} object to be stored
     * @throws BackendException when something is wrong with the URI
     */
    @Override
    public final void store(final MetaData metadata, final InputStream content) throws BackendException {
        // check if the table exists
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (dataSetInformation.isAttached()) {
            throw new BackendException("Data set is already attached, won't overwrite.");
        }
        // collect information from metadata
        final String uri = metadata.getDataUri();
        final String conPath = this.makeConnectionString(uri);
        final String user = this.getUserFromUri(uri);
        final String passwd = "";
        final String[] uriInfo = this.getServerDbTableFromUri(uri);
        final String tabName = uriInfo[2];
        final String dbName = uriInfo[1];
        /**
         * @TODO need to make this more flexible;
         */
        final String serverName = uriInfo[0];
        // get the header to be able to make a table
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        String firstLine = new String();
        try {
            firstLine = reader.readLine();
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        // get column headers, assume csv-like formatting
        String[] headers = firstLine.split(",");
        // store input stream as a file in HDFS
        MetaData metaTemp = new MetaData();
        metaTemp.put("data", new HashMap() {
            {
                put("uri", tabName);
            }
        });
        BackendModel backendModel = new BackendModel();
        backendModel.setName("store_helper");
        backendModel.setOptions(new HashMap());
        backendModel.getOptions().put("storagePath", "hdfs://" + serverName + ":" + HDFS_PORT);

        BackendHDFSImpl hdfsBackend = new BackendHDFSImpl(backendModel);
        hdfsBackend.store(metaTemp, content);
        // get driver
        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        //open connection
        try (Connection con = DriverManager.getConnection(conPath, user, passwd)) {
            Statement stmt = con.createStatement();
            // create a table using headers
            String sql = "CREATE TABLE " + tabName + "(";
            for (int iHead = 0; iHead < headers.length; iHead++) {
                if (iHead != headers.length - 1) {
                    sql += headers[iHead] + " STRING,";
                } else {
                    sql += headers[iHead] + " STRING";
                }
            }
            sql += ") ROW FORMAT DELIMITED FIELDS TERMINATED BY \',\' ";
            sql += "LINES TERMINATED by \'\\n\' STORED AS TEXTFILE";
            stmt.execute(sql);
            // write our local file to hive
            sql = "LOAD DATA INPATH  \'" + tabName + "\' OVERWRITE INTO TABLE " + tabName;
            stmt.execute(sql);
        }
        catch (SQLException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        // CREATE TABLE test_yahoo2
        // (Date STRING,Open STRING,High STRING,Low STRING,Close STRING,Volume STRING,Adj_Close STRING)
        // ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED by '\n' STORED AS TEXTFILE;

        // LOAD DATA INPATH  '/user/root/yahoo_stocks.csv' OVERWRITE INTO TABLE test_yahoo2;
    }

    /**
     * Reads the table specified by metadata. It first creates an file in HDFS
     * to which the table is stored. Then it uses {@link BackendHDFSInpl} to
     * open an input stream to this file. Note that in output, columns are
     * separated by CTRL-A
     *
     * @param metadata {@link MetaData} object containing the URI of origin
     * table
     * @return {@link InputStream} with the table content.
     * @throws BackendException if something is wrong with the URI
     */
    @Override
    public final InputStream read(final MetaData metadata) throws BackendException {
        // check if dataset really exists
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (!dataSetInformation.isAttached()) {
            throw new BackendException("No dataset attached.");
        }
        // collect information from metadata
        final String uri = metadata.getDataUri();
        final String conPath = this.makeConnectionString(uri);
        final String user = this.getUserFromUri(uri);
        final String passwd = "";
        final String[] uriInfo = this.getServerDbTableFromUri(uri);
        final String tabName = uriInfo[2];
        final String dbName = uriInfo[1];
        /**
         * @TODO need to make this more flexible;
         */
        final String serverName = uriInfo[0];
        // get driver
        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        //open connection
        try (Connection con = DriverManager.getConnection(conPath, user, passwd)) {
            Statement stmt = con.createStatement();
            // store table in HDFS
            String sql = "INSERT OVERWRITE DIRECTORY \'" + tabName + "\' ";
            sql += "SELECT * FROM " + dbName + "." + tabName;
            stmt.execute(sql);
            // Closing connection. The file will be read using HDFS backend
        }
        catch (SQLException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        // now make metadata pointing to a new file
        MetaData metaTemp = new MetaData();
        metaTemp.put("data", new HashMap() {
            {
                put("uri", tabName);
            }
        });
        // read it with HDFS backend
        BackendModel backendModel = new BackendModel();
        backendModel.setName("store_helper");
        backendModel.setOptions(new HashMap());
        backendModel.getOptions().put("storagePath", "hdfs://" + serverName + ":" + HDFS_PORT);

        BackendHDFSImpl hdfsBackend = new BackendHDFSImpl(backendModel);
        InputStream is = hdfsBackend.read(metaTemp);
        return is;

        //  INSERT OVERWRITE LOCAL DIRECTORY '/root/testDir3' ROW FORMAT DELIMITED
        // FIELDS TERMINATED BY ',' select * from default.test_yahoo2 limit 10;

        //   INSERT OVERWRITE DIRECTORY '/user/root/testDir4' select * from default.test_yahoo2 limit 10;
    }

    /**
     * Deletes table specified in metadata.
     *
     * @param metadata {@link MetaData} object containing the URI of table to be deleted
     * @return true if operation was success, false otherwise
     * @throws BackendException if there is something wrong with the metadata or
     * URI
     */
    @Override
    public final boolean delete(final MetaData metadata) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (!dataSetInformation.isAttached()) {
            throw new BackendException("No dataset attached.");
        }
        boolean success = false;
        // collect information from metadata
        final String uri = metadata.getDataUri();
        final String conPath = this.makeConnectionString(uri);
        final String user = this.getUserFromUri(uri);
        final String passwd = "";
        final String[] uriInfo = this.getServerDbTableFromUri(uri);
        final String tabName = uriInfo[2];
        final String dbName = uriInfo[1];
        // get driver
        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        //open connection
        try (Connection con = DriverManager.getConnection(conPath, user, passwd)) {
            Statement stmt = con.createStatement();
            String sql = "DROP TABLE " + dbName + "." + tabName;
            success = stmt.execute(sql);
        }
        catch (SQLException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
        //drop table test_yahoo2;
    }

}
