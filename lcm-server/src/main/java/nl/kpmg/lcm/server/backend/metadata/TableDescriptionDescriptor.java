/*
 * Copyright 2017 KPMG N.V. (unless otherwise stated).
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
package nl.kpmg.lcm.server.backend.metadata;

import nl.kpmg.lcm.server.data.metadata.AbstractMetaDataDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class TableDescriptionDescriptor extends AbstractMetaDataDescriptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TableDescriptionDescriptor.class
      .getName());

  public TableDescriptionDescriptor(MetaData metaData) {
    super(metaData);
  }

  private final Map getRawColumns() {
    return get("columns");
  }

  private final void setRawColumns(final Map columns) {
    set("columns", (Object) columns);
  }

  public void setColumns(Column[] columns) {

    Map columnsMap = new HashMap();
    for (Column column : columns) {
      ColumnMapDescriptor columnDescriptor = new ColumnMapDescriptor();
      columnDescriptor.setType(column.getType().getName());
      if (column.getColumnSize() != null && column.getColumnSize() != 0) {
        columnDescriptor.setSize(column.getColumnSize());
      }
      // TODO currently metamodel doesn't support precision for DECIMAL datatype
      // if it is important for you implement a workaround for example:
      // int precision = 10; // or laod it like a setting
      // columnDescription.put("precision", precision);
      columnsMap.put(column.getName(), columnDescriptor.getMap());
    }
    setRawColumns(columnsMap);
  }

  public void setColumns(List<ColumnDescription> columns) {

    Map columsMap = new HashMap();
    for (ColumnDescription column : columns) {
      ColumnMapDescriptor columnDescriptor = new ColumnMapDescriptor();
      columnDescriptor.setType(column.getType().getName());
      if (column.getSize() != null && column.getSize() != 0) {
        columnDescriptor.setSize(column.getSize());
      }
      if (column.getPrecision() != null && column.getPrecision() != 0) {
        columnDescriptor.setPrecision(column.getPrecision());
      }
      columsMap.put(column.getName(), columnDescriptor.getMap());
    }
    setRawColumns(columsMap);
  }

  public Map<String, ColumnDescription> getColumns() {

    Map<String, Map> metaDataColumns = get("columns");

    Map<String, ColumnDescription> columns = new HashMap<>();
    for (Map.Entry<String, Map> entry : metaDataColumns.entrySet()) {
      ColumnMapDescriptor columnDescriptor = new ColumnMapDescriptor(entry.getValue());

      if (columnDescriptor.getType() == null) {
        LOGGER.warn(String.format("Type of column %s is null", entry.getKey()));
      }

      ColumnType columnType = matchColumnType(columnDescriptor.getType());
      ColumnDescription column = new ColumnDescription(entry.getKey(), columnType);

      column.setSize(columnDescriptor.getSize());
      column.setPrecision(columnDescriptor.getPrecision());

      columns.put(entry.getKey(), column);
    }

    return columns;
  }

  /***
   * Trying to match the type of the input data to metamodel data system. If the type is not matched
   * then null is returned.
   *
   * @param value - Name of the type
   * @return
   */
  private ColumnType matchColumnType(String value) {
    ColumnType columnType = null;
    if (value != null) {
      columnType = ColumnTypeImpl.valueOf(value);
      // Add bellow all custom types that are not mached by metamodel.
      if (columnType == null) {
        if (value.equalsIgnoreCase("INT")) {
          columnType = ColumnType.INTEGER;
        }
      }
    }
    return columnType;
  }

  @Override
  public String getSectionName() {
    return "data.options.table-description";
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() == null) {
      notification.addError("Error: Section \"" + getSectionName()
          + "\" is not found in the metadata!");
      return;
    }
    validateField("columns", notification);
  }

}
