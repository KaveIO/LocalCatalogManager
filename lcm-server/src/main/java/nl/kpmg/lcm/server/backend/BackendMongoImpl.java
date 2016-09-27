/// *
// * Copyright 2016 KPMG N.V. (unless otherwise stated).
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
// package nl.kpmg.lcm.server.backend;
//
// import com.mongodb.CommandResult;
// import com.mongodb.DB;
// import com.mongodb.DBCollection;
// import com.mongodb.MongoClient;
// import java.io.InputStream;
// import java.net.URI;
// import java.net.URISyntaxException;
// import java.net.UnknownHostException;
// import java.util.Map;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import nl.kpmg.lcm.server.data.MetaData;
// import nl.kpmg.lcm.server.data.Storage;
//
/// **
// *
// * @author mhoekstra
// */
// public class BackendMongoImpl extends AbstractBackend {
//
// private Storage storage;
//
// public BackendMongoImpl(Storage storage) {
// this.storage = storage;
// }
//
// private DB createConnection() throws BackendException {
// Map options = storage.getOptions();
// try {
// if (!options.containsKey("host")
// || !options.containsKey("port")
// || !options.containsKey("database")
// || !options.containsKey("username")
// || !options.containsKey("password")) {
//
// throw new BackendException("Required parameter for mongo backend missing");
// }
//
// String host = (String) options.get("host");
// Integer port = (Integer) options.get("port");
// String database = (String) options.get("database");
// String username = (String) options.get("username");
// String password = (String) options.get("password");
//
// MongoClient mongoClient = new MongoClient(host, port);
// DB db = mongoClient.getDB(database);
// db.authenticate(username, password.toCharArray());
//
// return db;
// } catch (ClassCastException ex) {
// throw new BackendException("Failed creating mongo backend", ex);
// } catch (UnknownHostException ex) {
// throw new BackendException("Couldn't connecto to mongo backend", ex);
// }
// }
//
// private String getCollectionName(MetaData metadata) throws BackendException {
// String uri = metadata.getDataUri();
// if (uri == null || uri.isEmpty()) {
// return null;
// }
//
// URI parsedUri;
// try {
// parsedUri = new URI(uri);
// String path = parsedUri.getPath();
//
// if (path == null || path.isEmpty() || path.contains("/")) {
// throw new BackendException("Unparsable metadata url");
// }
//
// return path;
// } catch (URISyntaxException ex) {
// throw new BackendException("Unparsable metadata url", ex);
// }
// }
//
// @Override
// protected String getSupportedUriSchema() {
// return "mongo";
// }
//
// @Override
// public DataSetInformation gatherDataSetInformation(MetaData metadata) throws BackendException {
// DB connection = createConnection();
// String collectionName = getCollectionName(metadata);
//
// DataSetInformation dataSetInformation = new DataSetInformation();
// dataSetInformation.setUri(metadata.getDataUri());
//
// boolean collectionExists = connection.collectionExists(collectionName);
// if (collectionExists) {
// dataSetInformation.setAttached(true);
//
// DBCollection collection = connection.getCollection(collectionName);
// CommandResult stats = collection.getStats();
// dataSetInformation.setByteSize(stats.getInt("size"));
//
// if (collection.find().limit(1).count() == 1) {
// dataSetInformation.setReadable(true);
// }
// }
//
// return dataSetInformation;
// }
//
// @Override
// public void store(MetaData metadata, InputStream content) throws BackendException {
// throw new UnsupportedOperationException("Not supported yet.");
// }
//
// @Override
// public InputStream read(MetaData metadata) throws BackendException {
// throw new UnsupportedOperationException("Not supported yet.");
// }
//
// @Override
// public boolean delete(MetaData metadata) throws BackendException {
// throw new UnsupportedOperationException("Not supported yet.");
// }
//
// }
