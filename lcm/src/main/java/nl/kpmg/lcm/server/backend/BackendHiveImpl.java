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
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * Default constructor.
     *
     * @param server is the hive server address, e.g. 127.0.0.1
     */
    public BackendHiveImpl(final String server) {
        this.server = server;
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
            String path = dataUri.getPath().substring(1);
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
            ResultSet res = stmt.executeQuery("show tables in " + dbName);
            boolean isAttached = false;
            while (res.next() && !isAttached) {
                String resString = res.getString(1);
                if (resString.equals(tabName)) {
                    isAttached = true;
                }
            }
            dataSetInformation.setAttached(isAttached);
        }
        catch (SQLException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        // analyze table default.test_yahoo2 compute statistics;
        // describe formatted test_yahoo2
        return dataSetInformation;
    }

    @Override
    public void store(MetaData metadata, InputStream content) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        // CREATE TABLE test_yahoo2(Date STRING,Open STRING,High STRING,Low STRING,Close STRING,Volume STRING,Adj_Close STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED by '\n' STORED AS TEXTFILE
        // LOAD DATA INPATH  '/user/root/yahoo_stocks.csv' OVERWRITE INTO TABLE test_yahoo2;

    }

    /**
     * Executes query specified in metadata, saves output locally and opens
     *
     * @param metadata
     * @return
     * @throws BackendException
     */
    @Override
    public InputStream read(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //  INSERT OVERWRITE LOCAL DIRECTORY '/root/testDir3' ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' select * from default.test_yahoo2 limit 10;
        //   INSERT OVERWRITE DIRECTORY '/user/root/testDir4' select * from default.test_yahoo2 limit 10;
    }

    @Override
    public boolean delete(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //drop table test_yahoo2;
    }

}
