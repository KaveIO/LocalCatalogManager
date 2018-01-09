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
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.server.backend.metadata.ColumnDescription;
import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.backend.storage.MongoStorage;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.service.StorageService;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@BackendSource(type = {DataFormat.MONGO})
public class BackendMongoImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendMongoImpl.class.getName());

  private final TabularMetaData mongoMetaData;
  private UpdateableDataContext mongoContext = null;

  public BackendMongoImpl(MetaData metaData, StorageService service) {
    super(metaData, service);
    this.mongoMetaData = new TabularMetaData(metaData);
  }

  private UpdateableDataContext getDataContext(MongoStorage mongoStorage) {
    String password = mongoStorage.getPassword();
    return DataContextFactory.createMongoDbDataContext(mongoStorage.getHostname(),
        Integer.parseInt(mongoStorage.getPort()), mongoStorage.getDatabase(),
        mongoStorage.getUsername(), password.toCharArray());
  }

  @Override
  protected void enrichMetadataItem(EnrichmentProperties properties, String key) throws IOException {
    if (properties.getItemsCount() || properties.getStructure() || properties.getAccessibility()) {
      String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
      Storage storage = storageService.getStorageByUri(dataURI);
      MongoStorage mongoStorage = new MongoStorage(storage);
      UpdateableDataContext dataContext = getDataContext(mongoStorage);

      Schema database = dataContext.getSchemaByName(mongoStorage.getDatabase());
      if (database == null && properties.getAccessibility()) {
        mongoMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState("DETACHED");
        return;
      }
      Table table = database.getTableByName(getTableName(dataURI));
      if (table == null && properties.getAccessibility()) {
        mongoMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState("DETACHED");
        return;
      }

      if (properties.getAccessibility()) {
        mongoMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setState("ATTACHED");
      }

      if (properties.getItemsCount()) {
        Query query = dataContext.query().from(table).selectCount().toQuery();
        DataSet dataSet = dataContext.query().from(table).selectCount().execute();
        Row result = MetaModelHelper.executeSingleRowQuery(dataContext, query);
        Long count = (Long) result.getValue(0);
        mongoMetaData.getDynamicData().getDynamicDataDescriptor(key).getDetailsDescriptor()
            .setItemsCount(count);
      }

      if (properties.getStructure()) {
        mongoMetaData.getTableDescription(key).setColumns(table.getColumns());
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
    Storage storage = storageService.getStorageByUri(dataURI);
    MongoStorage mongoStorage = new MongoStorage(storage);

    UpdateableDataContext dataContext = getDataContext(mongoStorage);
    if (transferSettings == null) {
      transferSettings = new TransferSettings();
    }
    String tableName = getTableName(dataURI);

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
          crator.createTable(database, tableName, mongoMetaData.getTableDescription(key)
              .getColumns());
    }

    InsertInto insert = new InsertInto(table);
    Map<String, ColumnDescription> columns = mongoMetaData.getTableDescription(key).getColumns();
    String[] columnNames = (String[]) columns.keySet().toArray(new String[] {});
    while (content.hasNext()) {
      Map row = content.next();
      for (String columnName : columnNames) {
        insert = insert.value(columnName, row.get(columnName));
      }
    }
    dataContext.executeUpdate(insert);
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public IterativeData read(String key) {
    String dataURI = metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key).getURI();
    Storage storage = storageService.getStorageByUri(dataURI);
    MongoStorage mongoStorage = new MongoStorage(storage);

    UpdateableDataContext dataContext = getDataContext(mongoStorage);

    Schema schema = dataContext.getSchemaByName(mongoStorage.getDatabase());
    if (schema == null) {
      throw new LcmException("Error: database \"" + mongoStorage.getDatabase() + "\" is not found!");
    }
    // remove the first symbol as uri Path is something like "/tablex"
    String tableName = getTableName(dataURI);

    Table table = schema.getTableByName(tableName);

    if (table == null) {
      throw new LcmException("Error: specified table \"" + tableName
          + "\" in the metadata is not found!");
    }

    mongoMetaData.getTableDescription(key).setColumns(table.getColumns());

    DataSet dataSet = dataContext.query().from(table).selectAll().execute();
    LOGGER.info(String.format("Read from table: %s successfully", tableName));
    ContentIterator iterator = new DataSetContentIterator(dataSet);

    return new IterativeData(iterator);
  }

  @Override
  public void free() {

  }

  @Override
  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory) {
    this.progressIndicationFactory = progressIndicationFactory;

  }

    @Override
    protected List loadDataItems(String storageName, String subPath) {
        Storage storage = storageService.findByName(storageName);
        MongoStorage mongoStorage = new MongoStorage(storage);

    UpdateableDataContext dataContext = getDataContext(mongoStorage);

    Schema database = dataContext.getSchemaByName(mongoStorage.getDatabase());
    if (database == null) {
      return null;
    }

    return new ArrayList(Arrays.asList(database.getTableNames()));
    }

}
