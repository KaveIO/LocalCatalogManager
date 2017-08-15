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
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.exception.LcmException;

import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.create.CreateTableColumnBuilder;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class TableCreator {
  private final UpdateableDataContext dataContext;
  private final TransferSettings transformationSettings;
  private static final Logger logger = LoggerFactory.getLogger(TableCreator.class.getName());

  public TableCreator(UpdateableDataContext dataContext,
      TransferSettings transformationSettings) {
    this.dataContext = dataContext;
    if (transformationSettings != null) {
      this.transformationSettings = transformationSettings;
    } else {
      this.transformationSettings = new TransferSettings();
    }
  }

  public Table createTable(Schema database, String tableName,
      Map<String, ColumnDescription> columns) {

    CreateTable createTable = new CreateTable(database, tableName);
    if (columns == null) {
      throw new LcmException(
          "There is no \"table-description\" section in the metadata! At least column names are required.");
    }

    fillCreateTable(columns, createTable);

    dataContext.executeUpdate(createTable);
    Table table = database.getTableByName(tableName);
    if (table == null) {
      throw new LcmException("Unable to create table with name: " + tableName);
    }

    return table;
  }

  private void fillCreateTable(Map<String, ColumnDescription> columns, CreateTable createTable) {

    for (Map.Entry<String, ColumnDescription> entry : columns.entrySet()) {
      ColumnType columnType = entry.getValue().getType();

      // In case that there is no type we adopt varchar
      // this could happen when the original data source doesn't support types i.e. CSV
      if (columnType == null) {
        logger.warn(String.format("Missing type for column %s. Varchar will be used further.",
            entry.getKey()));
        columnType = ColumnType.VARCHAR;
      }

      Integer size = entry.getValue().getSize();

      // Varchar columns could not be without size.
      if ((size == null || size == 0)
          && (columnType == ColumnType.VARCHAR || columnType == ColumnType.STRING)) {
        size = transformationSettings.getVarCharSize();
      }

      // Currently Decimal is supported poorly by Hive
      if (columnType == ColumnType.DECIMAL) {
        Integer precision = entry.getValue().getPrecision();
        if (precision != null) {
          // Set native type because CreateTable dosn't support precision.
          createTable.withColumn(entry.getKey())
              .ofNativeType(columnType.getName() + "(" + size + "," + precision + ")");

          continue;
        } else {
          // In case there is no precision specified convert the column to string
          // reserve for precision transformationSettings.getDecimalPrecision() size.
          // Caution! It is possible to lose presion here!
          logger.warn(String.format("Column \"%s\" has tpye \"DECIMAL\" but no precision. "
                  + "The fields will be converted to varchar and default precision will be used.",
                  entry.getKey()));
          columnType = ColumnType.VARCHAR;
          size = size + transformationSettings.getDecimalPrecision() + 1;
        }
      }

      CreateTableColumnBuilder builder = createTable.withColumn(entry.getKey()).ofType(columnType);

      if (size != null && size != 0) {
        builder.ofSize(size);
      }
    }
  }

}
