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
package nl.kpmg.lcm.server.backend.metatadata;

import nl.kpmg.lcm.server.backend.ColumnDescription;
import nl.kpmg.lcm.server.data.meatadata.MetaData;
import nl.kpmg.lcm.server.data.meatadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmException;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
// TODO Refactor this class. Add all needed sub objects
// like table description, columns etc.
public class TabularMetaData extends MetaDataWrapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(TabularMetaData.class.getName());

  public TabularMetaData(MetaData metaData) {
    super(metaData);
  }

  public void addColumnsDescription(Column[] columnNames) {

    for (Column column : columnNames) {
      String name = "data.options.table-description.columns." + column.getName();
      Map columnDescription = new HashMap<>();
      columnDescription.put("type", column.getType().getName());
      if (column.getColumnSize() != null && column.getColumnSize() != 0) {
        columnDescription.put("size", column.getColumnSize());
        // TODO currently metamodel doesn't support precision for DECIMAL datatype
        // if it is important for you implement a workaround for example:
        // int precision = 10; // or laod it like a setting
        // columnDescription.put("precision", precision);
      }
      metaData.set(name, columnDescription);
    }
  }


  public Map<String, ColumnDescription> getColumns() {

    Map<String, Map> metaDataColumns = getColumnsMap();

    Map<String, ColumnDescription> columns = new HashMap<>();
    for (Map.Entry<String, Map> entry : metaDataColumns.entrySet()) {
      Map<String, Object> columnDescription = entry.getValue();
      String type = (String) columnDescription.get("type");
      if (type == null) {
        LOGGER.warn("Type of column {0} is null", entry.getKey());
      }
      ColumnType columnType = matchColumnType(type);
      ColumnDescription column = new ColumnDescription(entry.getKey(), columnType);
      if ((String) columnDescription.get("size") != null) {
        Integer size = toInteger(columnDescription.get("size"));
        column.setSize(size);
      }
      if ((String) columnDescription.get("precision") != null) {
        Integer precision = toInteger(columnDescription.get("precision"));
        column.setPrecision(precision);
      }

      columns.put(entry.getKey(), column);
    }

    return columns;
  }


  private Integer toInteger(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Integer) {
      return (Integer) value;
    } else {
      LOGGER.warn("Unable to convert metadata value: {0}.", value);
      return null;
    }
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

  private Map<String, Map> getColumnsMap() {
    Map dataOptions = getData().getOptions();
    if (dataOptions == null) {
      LOGGER.warn("Can not construct columns as \"data.option\" field is null");
      return null;
    }

    Map<String, Map> metaDataColumns = null;
    if (dataOptions.containsKey("table-description")) {
      metaDataColumns = getMetaData().get("data.options.table-description.columns");
    } else {
      throw new LcmException(
          "There is no \"table-description\" section in the metadata! At least column names are required.");
    }

    return metaDataColumns;
  }
}
