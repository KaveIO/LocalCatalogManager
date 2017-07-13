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
import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.server.data.IterativeData;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.TransferSettings;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.exception.LcmException;

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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
  public MetaData enrichMetadata(EnrichmentProperties enrichment) {
    expandDataURISection();
    if (metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors() == null) {
      return metaDataWrapper.getMetaData();
    }
    for (String key : metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors().keySet()) {
    long start = System.currentTimeMillis();
    try {
      hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).clearDetailsDescriptor();
      if (enrichment.getItemsCount() || enrichment.getStructure() || enrichment.getAccessibility()) {
         
        String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
        Storage storage = storageService.getStorageByUri(dataURI);
        HiveStorage hiveStorage =  new HiveStorage(storage);  
        JdbcDataContext dataContext = getDataContext(hiveStorage);

        Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
        if (database == null) {
          if (enrichment.getAccessibility()) {
            hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setState("DETACHED");
          }
          return hiveMetaData.getMetaData();
        }
        Table table = database.getTableByName(getTableName(dataURI));
        if (table == null) {
          if (enrichment.getAccessibility()) {
            hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setState("DETACHED");
          }
          return hiveMetaData.getMetaData();
        }

        if (enrichment.getAccessibility()) {
          hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setState("ATTACHED");
        }

        if (enrichment.getItemsCount()) {
          Query query = dataContext.query().from(table).selectCount().toQuery();
          DataSet dataSet = dataContext.query().from(table).selectCount().execute();
          Row result = MetaModelHelper.executeSingleRowQuery(dataContext, query);
          Long count = (Long) result.getValue(0);
          hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setItemsCount(count);
        }

        if (enrichment.getStructure()) {
          hiveMetaData.getTableDescription(key).setColumns(table.getColumns());
        }

      }
    } catch (Exception ex) {
      LOGGER.error("Unable to enrich medatadata : " + hiveMetaData.getId() + ". Error Message: "
          + ex.getMessage());
      throw new LcmException("Unable to enrich medatadata : " + hiveMetaData.getId(), ex);
    } finally {
      hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setUpdateTimestamp(new Date().getTime());
      long end = System.currentTimeMillis();
      hiveMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor().setUpdateDurationTimestamp(end - start);
    }
    }
    return hiveMetaData.getMetaData();
  }

  @Override
  public void store(Data data, String key, TransferSettings transferSettings) {

    if (!(data instanceof IterativeData)) {
      throw new LcmException("Unable to store streaming data directly to hive storage.");
    }

    ContentIterator content = ((IterativeData) data).getIterator();

    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    Storage storage = storageService.getStorageByUri(dataURI);
    HiveStorage hiveStorage =  new HiveStorage(storage);
    JdbcDataContext dataContext = getDataContext(hiveStorage);
    if (transferSettings == null) {
      transferSettings = new TransferSettings();
    }
    String tableName = getTableName(dataURI);

    Schema database = dataContext.getSchemaByName(hiveStorage.getDatabase());
    if(database == null) {
        throw new LcmException("Error, can not store the data! Database: \"" + hiveStorage.getDatabase()
          + "\" does not exist!");
    }

    Table table = database.getTableByName(tableName);

    if (table != null && !transferSettings.isForceOverwrite()) {
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
    throw new UnsupportedOperationException("Backend delete operation is not supported yet.");
  }

  @Override
  public IterativeData read(String key) {
    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    Storage storage = storageService.getStorageByUri(dataURI);
    HiveStorage hiveStorage =  new HiveStorage(storage);
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
}
