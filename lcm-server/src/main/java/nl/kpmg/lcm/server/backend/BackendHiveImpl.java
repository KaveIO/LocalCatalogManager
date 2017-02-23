/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.backend;

import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.backend.storage.HiveStorage;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@BackendSource(type = "hive")
public class BackendHiveImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendHiveImpl.class.getName());

  private final HiveStorage hiveStorage;
  private final TabularMetaData hiveMetaData;

  private Connection connection;

  public BackendHiveImpl(Storage backendStorage, MetaData metaData) {
    super(metaData);
    this.hiveStorage = new HiveStorage(backendStorage);
    this.hiveMetaData = new TabularMetaData(metaData);
  }

  private Connection getConnection() {
    final String className = getClass().getName();

    if (connection != null) {
      return connection;
    }

    try {
      Class.forName(hiveStorage.getDriver());
      connection = DriverManager.getConnection(hiveStorage.getUrl(), hiveStorage.getUsername(),
          hiveStorage.getPassword());
    } catch (Exception e) {
      throw new LcmException("Failed to create JDBC connection for " + className, e);
    }

    return connection;
  }

  private JdbcDataContext getDataContext() {
    return new JdbcDataContext(getConnection());
  }

  @Override
  protected String getSupportedUriSchema() {
    return "hive";
  }

  @Override
  public DataSetInformation gatherDataSetInformation() {

    JdbcDataContext dataContext = getDataContext();

    DataSetInformation info = new DataSetInformation();

    String tableName = getDataUri().getPath();
    info.setUri(tableName);

    Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
    Table table = database.getTableByName(tableName);
    if (table == null) {
      info.setAttached(false);
    } else {
      info.setAttached(true);
    }

    return info;
  }

  @Override
  public void store(ContentIterator content, DataTransformationSettings transformationSettings,
      boolean forceOverwrite) {
    JdbcDataContext dataContext = getDataContext();
    if (transformationSettings == null) {
      transformationSettings = new DataTransformationSettings();
    }
    // remove the first symbol as uri Path is something like "/tablex"
    String tableName = getDataUri().getPath().substring(1);
    if (tableName.contains(".")) {
      tableName = tableName.replace(".", "_");
    }

    Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
    Table table = database.getTableByName(tableName);

    if (table != null && !forceOverwrite) {
      throw new LcmException("Error, can not store the data! Table: \"" + tableName
          + "\" already exists and storing is started without overwriting!");
    }

    if (table != null && forceOverwrite) {
      DropTable dropTable = new DropTable(database, tableName);
      dataContext.executeUpdate(dropTable);
      table = null;
    }

    if (table == null) {
      TableCreator crator = new TableCreator(dataContext, transformationSettings);
      crator.createTable(database, tableName, hiveMetaData.getTableDescription().getColumns());
    }

    try {
      JdbcMultipleRowsWriter hiveWriter = new JdbcMultipleRowsWriter(connection, tableName,
          hiveStorage.getDatabase(), hiveMetaData.getTableDescription().getColumns());

      hiveWriter.write(content, transformationSettings.getMaximumInsertedRecordsPerQuery());

    } catch (SQLException ex) {
      throw new LcmException("Unable to stora the data: ", ex);
    }
  }

  @Override
  public boolean delete() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void extraValidation(MetaDataWrapper metaDataWrapper, Notification notification) {

  }

  @Override
  public Data read()  {
    JdbcDataContext dataContext = getDataContext();

    Schema schema = dataContext.getSchemaByName(hiveStorage.getDatabase());
    if (schema == null) {
      throw new LcmException(
          "Error: database \"" + hiveStorage.getDatabase() + "\" is not found!");
    }
    // remove the first symbol as uri Path is something like "/tablex"
    String tableName = getDataUri().getPath().substring(1);

    Table table = schema.getTableByName(tableName);

    if (table == null) {
      throw new LcmException(
          "Error: specified table \"" + tableName + "\" in the metadata is not found!");
    }

    hiveMetaData.getTableDescription().setColumns(table.getColumns());

    DataSet dataSet = dataContext.query().from(table).selectAll().execute();
    LOGGER.info(String.format("Read from table: %s sucessfully", tableName));
    ContentIterator iterator = new DataSetContentIterator(dataSet);

    return new Data(hiveMetaData.getMetaData(), iterator);
  }

  @Override
  public void free() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException ex) {
        LOGGER.error( "Unsble to close connection.", ex);
      }
      connection = null;
    }
  }
}
