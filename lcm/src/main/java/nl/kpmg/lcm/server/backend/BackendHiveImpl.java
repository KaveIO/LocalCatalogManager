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
 * Backend for Hive 2 server
 *
 * @author jpavel
 */
public class BackendHiveImpl extends AbstractBackend {

    /**
     * Address of the Hive server.
     *
     * @param storagePath is the server address.
     */
    private final String server;
    private final String URIscheme;
    private final String port;
    private final String dbName;
    private final String user;
    private final String passwd;

    private final static String driverName = "org.apache.hive.jdbc.HiveDriver";

    /**
     * Default constructor.
     *
     * @param server
     * @param dbName
     * @param storagePath is the server address.
     */
    public BackendHiveImpl(final String uri) throws BackendException {
        String[] uriInfo = this.getServerDbTableFromUri(uri);
        this.server = uriInfo[0];
        this.URIscheme = "jdbc:hive2://";
        this.port = this.getPortFromUri(uri);
        this.dbName = uriInfo[1];
        this.user = this.getUserFromUri(uri);
        this.passwd = "";
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
     * Creates the full path for the connection from the Backend members
     *
     * @return
     */
    private String makeConnectionString() {
        String out = this.URIscheme + this.server + ":" + this.port + "/" + this.dbName;
        return out;
    }

    private String makeConnectionString(String uri) throws BackendException {
        String[] uriInfo = this.getServerDbTableFromUri(uri);
        String uriPort = this.getPortFromUri(uri);
        String out = "jdbc:hive2://" + uriInfo[0] + ":" + uriPort + "/" + uriInfo[1];
        return out;
    }

    /**
     * Gathers the host name, dbname and table name from the uri
     *
     * @param uri
     * @return
     * @throws BackendException
     */
    private String[] getServerDbTableFromUri(final String uri) throws BackendException {

        String[] out = new String[3];
        if (uri != null) {
            URI dataUri;
            dataUri = parseUri(uri);
            out[0] = dataUri.getHost();
            String path = dataUri.getPath().substring(1);
            String[] path_parts = path.split("/", 0);
            if (path_parts.length != 2) {
                throw new BackendException("Wrong URI format. Please use hive://[user]@host:port/DBname/TableName");
            }
            out[1] = path_parts[0];
            out[2] = path_parts[1];
        } else {
            throw new BackendException("Empty URI!");
        }
        return out;
    }

    /**
     * Gets port number from the URI
     *
     * @param uri
     * @return
     * @throws BackendException
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

    @Override
    public DataSetInformation gatherDataSetInformation(MetaData metadata) throws BackendException {
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
            Class.forName(driverName);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        try(Connection con = DriverManager.getConnection(conPath, user, passwd)){
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("show tables in "+dbName);
            boolean isAttached = false;
            while (res.next()) {
                if(res.getString(1)==tabName) isAttached = true;
            }
            dataSetInformation.setAttached(isAttached);
//         System.out.println(res.getString(1)+" "+res.getString(2)+" "+res.getString(3));
//        }
            
        } catch (SQLException ex) {
            Logger.getLogger(BackendHiveImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        // analyze table default.test_yahoo2 compute statistics;
        // describe formatted test_yahoo2
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
