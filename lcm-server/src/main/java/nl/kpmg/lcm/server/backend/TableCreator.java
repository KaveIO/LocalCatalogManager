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

import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.backend.exception.BadMetaDataException;

import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.create.CreateTableColumnBuilder;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class TableCreator {
  private final JdbcDataContext dataContext;
  private final DataTransformationSettings transformationSettings;
  private static final Logger logger = Logger.getLogger(TableCreator.class.getName());

  public TableCreator(JdbcDataContext dataContext,
      DataTransformationSettings transformationSettings) {
    this.dataContext = dataContext;
    if (transformationSettings != null) {
      this.transformationSettings = transformationSettings;
    } else {
      this.transformationSettings = new DataTransformationSettings();
    }
  }

  public Table createTable(Schema database, String tableName,
      Map<String, ColumnDescription> columns) throws BackendException {

    CreateTable createTable = new CreateTable(database, tableName);
    if (columns == null) {
      throw new BadMetaDataException(
          "There is no \"table-description\" section in the metadata! At least column names are required.");
    }

    fillCreateTable(columns, createTable);

    dataContext.executeUpdate(createTable);
    Table table = database.getTableByName(tableName);
    if (table == null) {
      throw new BackendException("Unable to create table with name: " + tableName);
    }

    return table;
  }

  private void fillCreateTable(Map<String, ColumnDescription> columns, CreateTable createTable)
      throws BackendException {

    for (Map.Entry<String, ColumnDescription> entry : columns.entrySet()) {
      ColumnType columnType = entry.getValue().getType();

      // In case that there is no type we adopt varchar
      // this could happen when the original data source doesn't support types i.e. CSV
      if (columnType == null) {
        logger.log(Level.WARNING, "Missing type for column {0}. Varchar will be used further.",
            entry.getKey());
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
          columnType = ColumnType.VARCHAR;
          size = size + transformationSettings.getDecimalPrecision() + 1;
        }
      }

      CreateTableColumnBuilder builder = createTable.withColumn(entry.getKey()).ofType(columnType);

      if (size != null || size != 0) {
        builder.ofSize(size);
      }
    }
  }

}