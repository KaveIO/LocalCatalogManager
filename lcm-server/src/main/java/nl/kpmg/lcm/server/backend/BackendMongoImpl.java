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

import nl.kpmg.lcm.server.backend.metadata.ColumnDescription;
import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.backend.storage.MongoStorage;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.server.data.IterativeData;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.TransferSettings;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.exception.LcmException;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@BackendSource(type = {DataFormat.MONGO})
public class BackendMongoImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendMongoImpl.class.getName());

  private final MongoStorage mongoStorage;
  private final TabularMetaData mongoMetaData;
  private UpdateableDataContext mongoContext = null;

  public BackendMongoImpl(Storage backendStorage, MetaData metaData) {
    super(metaData);
    this.mongoStorage = new MongoStorage(backendStorage);
    this.mongoMetaData = new TabularMetaData(metaData);
  }

  private UpdateableDataContext getDataContext() {
    if (mongoContext == null) {
      String password = mongoStorage.getPassword();
      mongoContext =
          DataContextFactory.createMongoDbDataContext(mongoStorage.getHostname(),
              Integer.parseInt(mongoStorage.getPort()), mongoStorage.getDatabase(),
              mongoStorage.getUsername(), password.toCharArray());
    }

    return mongoContext;
  }

  @Override
  public MetaData enrichMetadata(EnrichmentProperties enrichment) {
    long start = System.currentTimeMillis();
    try {
      mongoMetaData.clearDynamicData();
      if (enrichment.getItemsCount() || enrichment.getStructure() || enrichment.getAccessibility()) {
        UpdateableDataContext dataContext = getDataContext();

        Schema database = dataContext.getSchemaByName(mongoStorage.getDatabase());
        if (database == null) {
          if (enrichment.getAccessibility()) {
            mongoMetaData.getDynamicData().setState("DETACHED");
          }
          return mongoMetaData.getMetaData();
        }
        Table table = database.getTableByName(getTableName());
        if (table == null) {
          if (enrichment.getAccessibility()) {
            mongoMetaData.getDynamicData().setState("DETACHED");
          }
          return mongoMetaData.getMetaData();
        }

        if (enrichment.getAccessibility()) {
          mongoMetaData.getDynamicData().setState("ATTACHED");
        }

        if (enrichment.getItemsCount()) {
          Query query = dataContext.query().from(table).selectCount().toQuery();
          DataSet dataSet = dataContext.query().from(table).selectCount().execute();
          Row result = MetaModelHelper.executeSingleRowQuery(dataContext, query);
          Long count = (Long) result.getValue(0);
          mongoMetaData.getDynamicData().setItemsCount(count);
        }

        if (enrichment.getStructure()) {
          mongoMetaData.getTableDescription().setColumns(table.getColumns());
        }

      }
    } catch (Exception ex) {
      LOGGER.error("Unable to enrich medatadata : " + mongoMetaData.getId() + ". Error Message: "
          + ex.getMessage());
      throw new LcmException("Unable to enrich medatadata : " + mongoMetaData.getId(), ex);
    } finally {
      mongoMetaData.getDynamicData().setUpdateTimestamp(new Date().getTime());
      long end = System.currentTimeMillis();
      mongoMetaData.getDynamicData().setUpdateDurationTimestamp(end - start);
    }
    return mongoMetaData.getMetaData();
  }

  @Override
  public void store(Data data, TransferSettings transferSettings) {

    if (!(data instanceof IterativeData)) {
      throw new LcmException("Unable to store streaming data directly to hive storage.");
    }

    ContentIterator content = ((IterativeData) data).getIterator();
    UpdateableDataContext dataContext = getDataContext();
    if (transferSettings == null) {
      transferSettings = new TransferSettings();
    }
    String tableName = getTableName();

    Schema database = dataContext.getSchemaByName(mongoStorage.getDatabase());
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
      table =
          crator.createTable(database, tableName, mongoMetaData.getTableDescription().getColumns());
    }

    InsertInto insert = new InsertInto(table);
    Map<String, ColumnDescription> columns = mongoMetaData.getTableDescription().getColumns();
    String[] columnNames = (String[]) columns.keySet().toArray(new String[] {});
    while (content.hasNext()) {
      Map row = content.next();
      for (String columnName : columnNames) {
        insert = insert.value(columnName, row.get(columnName));
      }
    }
    getDataContext().executeUpdate(insert);
  }

  private String getTableName() {
    // remove the first symbol as uri Path is something like "/tableX"
    String tableName = mongoMetaData.getData().getStorageItemName().substring(1);
    if (tableName.contains(".")) {
      tableName = tableName.replace(".", "_");
    }
    return tableName;
  }

  @Override
  public boolean delete() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public IterativeData read() {
    UpdateableDataContext dataContext = getDataContext();

    Schema schema = dataContext.getSchemaByName(mongoStorage.getDatabase());
    if (schema == null) {
      throw new LcmException("Error: database \"" + mongoStorage.getDatabase() + "\" is not found!");
    }
    // remove the first symbol as uri Path is something like "/tablex"
    String tableName = getTableName();

    Table table = schema.getTableByName(tableName);

    if (table == null) {
      throw new LcmException("Error: specified table \"" + tableName
          + "\" in the metadata is not found!");
    }

    mongoMetaData.getTableDescription().setColumns(table.getColumns());

    DataSet dataSet = dataContext.query().from(table).selectAll().execute();
    LOGGER.info(String.format("Read from table: %s successfully", tableName));
    ContentIterator iterator = new DataSetContentIterator(dataSet);

    return new IterativeData(mongoMetaData.getMetaData(), iterator);
  }

  @Override
  public void free() {

  }

  @Override
  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory) {
    this.progressIndicationFactory = progressIndicationFactory;

  }
}