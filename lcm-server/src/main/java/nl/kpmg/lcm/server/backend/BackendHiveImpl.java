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

import nl.kpmg.lcm.common.data.ContentIterator;
import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.DataState;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.backend.storage.HiveStorage;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.service.StorageService;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@BackendSource(type = {DataFormat.HIVE})
public class BackendHiveImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendHiveImpl.class.getName());

  private final TabularMetaData hiveMetaData;

  private Connection connection;

  public BackendHiveImpl(MetaData metaData, StorageService service) {
    super(metaData, service);
    this.hiveMetaData = new TabularMetaData(metaData);
  }

  private Connection getConnection(HiveStorage hiveStorage) {
    final String className = getClass().getName();

    if (connection != null) {
      return connection;
    }

    try {
      Class.forName(hiveStorage.getDriver());
      connection =
          DriverManager.getConnection(hiveStorage.getUrl(), hiveStorage.getUsername(),
              hiveStorage.getPassword());
    } catch (Exception e) {
      throw new LcmException("Failed to create JDBC connection for " + className, e);
    }

    return connection;
  }

  private JdbcDataContext getDataContext(HiveStorage hiveStorage) {
    return new JdbcDataContext(getConnection(hiveStorage));
  }

  @Override
  protected void enrichMetadataItem(EnrichmentProperties properties, String key) throws IOException {
    hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).clearDetailsDescriptor();
    if (properties.getItemsCount() || properties.getStructure() || properties.getAccessibility()) {
      String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
      HiveStorage hiveStorage = getHiveStorage(dataURI);
      JdbcDataContext dataContext = null;
      try {
        dataContext = getDataContext(hiveStorage);
      } catch (LcmException ex) {
        hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState(DataState.DETACHED);
        LOGGER.warn("The metadata with id: " + metaDataWrapper.getId()
            + " has problems with the connection. " + ex.getMessage());
        return;
      }

      Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
      if (database == null && properties.getAccessibility()) {
        hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState(DataState.DETACHED);
        return;
      }
      Table table = database.getTableByName(getTableName(dataURI));
      if (table == null && properties.getAccessibility()) {
        hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState(DataState.DETACHED);
        return;
      }

      if (properties.getAccessibility()) {
        hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState(DataState.ATTACHED);
      }

      if (properties.getItemsCount()) {
        Query query = dataContext.query().from(table).selectCount().toQuery();
        DataSet dataSet = dataContext.query().from(table).selectCount().execute();
        Row result = MetaModelHelper.executeSingleRowQuery(dataContext, query);
        Long count = (Long) result.getValue(0);
        hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setItemsCount(count);
      }

      if (properties.getStructure()) {
        hiveMetaData.getTableDescription(key).setColumns(table.getColumns());
      }
    }
  }

  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {

    if (!(data instanceof IterativeData)) {
      throw new LcmException("Unable to store streaming data directly to hive storage.");
    }

    ContentIterator content = ((IterativeData) data).getIterator();

    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    HiveStorage hiveStorage =  getHiveStorage(dataURI);
    JdbcDataContext dataContext = getDataContext(hiveStorage);
    if (transferSettings == null) {
      transferSettings = new TransferSettings();
    }
    String tableName = getTableName(dataURI);

    Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
    if(database == null) {
        if (progressIndicationFactory != null) {
            String message = "Database: \"" + hiveStorage.getDatabase() + "\" does not exist!";
            progressIndicationFactory.writeIndication(message);
        }
        throw new LcmException("Error, can not store the data! Database: \"" + hiveStorage.getDatabase()
          + "\" does not exist!");
    }

    Table table = database.getTableByName(tableName);

    if (table != null && !transferSettings.isForceOverwrite()) {
      if (progressIndicationFactory != null) {
        String message = "Table: \"" + tableName
                + "\" already exists and storing is started without overwriting!";
        progressIndicationFactory.writeIndication(message);
      }
      throw new LcmException("Error, can not store the data! Table: \"" + tableName
              + "\" already exists and storing is started without overwriting!");
    }

    if (table != null && transferSettings.isForceOverwrite()) {
      DropTable dropTable = new DropTable(database, tableName);
      dataContext.executeUpdate(dropTable);
      table = null;
    }

    if (table == null) {
      TableCreator crator = new TableCreator(dataContext, transferSettings);
      crator.createTable(database, tableName, hiveMetaData.getTableDescription(key).getColumns());
    }

    try {
      JdbcMultipleRowsWriter hiveWriter =
          new JdbcMultipleRowsWriter(connection, tableName, hiveStorage.getDatabase(), hiveMetaData
              .getTableDescription(key).getColumns());
      hiveWriter.setProgressIndicationFactory(progressIndicationFactory);

      hiveWriter.write(content, transferSettings.getMaximumInsertedRecordsPerQuery());
    } catch (SQLException ex) {
      throw new LcmException("Unable to stora the data: ", ex);
    }
  }

  private String getTableName(String uri) {
    String storageItem = storageService.getStorageItemName(uri);
    // remove the first symbol as uri Path is something like "/tableX"
    String tableName = storageItem.substring(1);
    if (tableName.contains(".")) {
      tableName = tableName.replace(".", "_");
    }
    return tableName;
  }

  @Override
  public boolean delete(String key) {
    try {
      String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
      HiveStorage hiveStorage = getHiveStorage(dataURI);
      JdbcDataContext dataContext = getDataContext(hiveStorage);

      Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
      if (database == null) {
        throw new LcmException("Error, can not store the data! Database: \""
            + hiveStorage.getDatabase() + "\" does not exist!");
      }

      String tableName = getTableName(dataURI);
      Table table = database.getTableByName(tableName);
      if (table == null) {
        throw new LcmException("Error: specified table \"" + tableName
            + "\" in the metadata is not found!");
      }
      DropTable dropTable = new DropTable(database, tableName);
      dataContext.executeUpdate(dropTable);
    } catch (Exception ex) {
      LOGGER.warn("Unable to delete hive table. Error: " + ex.getMessage());
      return false;
    }

    return true;
  }

  @Override
  public IterativeData read(String key) {
    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    HiveStorage hiveStorage =  getHiveStorage(dataURI);
    JdbcDataContext dataContext = getDataContext(hiveStorage);

    Schema schema = dataContext.getSchemaByName(hiveStorage.getDatabase());
    if (schema == null) {
      throw new LcmException("Error: database \"" + hiveStorage.getDatabase() + "\" is not found!");
    }
    // remove the first symbol as uri Path is something like "/tablex"
    String tableName = getTableName(dataURI);

    Table table = schema.getTableByName(tableName);

    if (table == null) {
      throw new LcmException("Error: specified table \"" + tableName
          + "\" in the metadata is not found!");
    }

    hiveMetaData.getTableDescription(key).setColumns(table.getColumns());

    DataSet dataSet = dataContext.query().from(table).selectAll().execute();
    LOGGER.info(String.format("Read from table: %s successfully", tableName));
    ContentIterator iterator = new DataSetContentIterator(dataSet);

    return new IterativeData(iterator);
  }

  @Override
  public void free() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException ex) {
        LOGGER.error("Unsble to close connection.", ex);
      }
      connection = null;
    }
  }

  @Override
  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory) {
    this.progressIndicationFactory = progressIndicationFactory;

  }

  @Override
  protected List loadDataItems(String storageName, String subPath) {
    Storage storage = storageService.findByName(storageName);
    HiveStorage hiveStorage = new HiveStorage(storage);
    JdbcDataContext dataContext = getDataContext(hiveStorage);

    Schema schema = dataContext.getSchemaByName(hiveStorage.getDatabase());
    if (schema == null) {
      return null;
    }

    return new ArrayList(Arrays.asList(schema.getTableNames()));
  }

  private HiveStorage getHiveStorage(String dataURI) {
    Storage storage = storageService.getStorageByUri(dataURI);
    HiveStorage hiveStorage = new HiveStorage(storage);
    return hiveStorage;
  }
}
