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
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
class JdbcMultipleRowsWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcMultipleRowsWriter.class.getName());
  private ProgressIndicationFactory progressIndicationFactory;

  private final String tableName;
  private final String dbName;
  private final Connection connection;
  private final Map<String, ColumnDescription> columns;

  public JdbcMultipleRowsWriter(Connection connection, String tableName, String dbName,
      Map<String, ColumnDescription> columns) {
    this.tableName = tableName;
    this.dbName = dbName;
    this.connection = connection;
    this.columns = columns;
  }

  public void write(ContentIterator content, int maximumInsertedRowsPerQuery) throws SQLException {

    String[] columnNames = (String[]) columns.keySet().toArray(new String[] {});
    String maximumQuery = constructQuery(maximumInsertedRowsPerQuery, columnNames);
    int totalCount = 0;
    if (progressIndicationFactory != null) {
      String message = "Start transfer.";
      progressIndicationFactory.writeIndication(message);
    }
    while (content.hasNext()) {

      List<Map> rows = getRowsToInsert(content, maximumInsertedRowsPerQuery);

      String query;
      if (rows.size() == maximumInsertedRowsPerQuery) {
          query = maximumQuery;
      } else {
        query = constructQuery(rows.size(), columnNames);
      }


      PreparedStatement pst = null;
      try {
        pst = createPrepareStatement(query, rows);
        pst.executeUpdate();
        totalCount += rows.size();
        if(progressIndicationFactory != null ) {
            String message = String.format("Written successfully %d rows in table: %s",
            totalCount, tableName);
            progressIndicationFactory.writeIndication(message);
        }
      } catch (SQLException ex) {
        if (totalCount > 0) {
          if (progressIndicationFactory != null) {
            String message =
                String.format("The content is inserted partially, only %d rows in table: %s",
                    totalCount, tableName);
            progressIndicationFactory.writeIndication(message);
          }
        }
        LOGGER.warn(String.format("Unable to execute query starting with : %s",
            query.substring(0, 300)));
        throw ex;
      } finally {
          if(pst !=  null) {
            pst.close();
          }
      }
    }

    if (progressIndicationFactory != null) {
      String message =
          String.format("All the content inserted successfully %d rows in table: %s", totalCount,
              tableName);
      progressIndicationFactory.writeIndication(message);
    }
  }

  private List<Map> getRowsToInsert(ContentIterator content, int maximumInsertedRowsPerQuery) {

    int counter = 0;
    List<Map> rows = new ArrayList();
    while (content.hasNext() && counter < maximumInsertedRowsPerQuery) {
      counter++;
      rows.add(content.next());
    }
    return rows;
  }

  private String constructQuery(int rowsCount, String[] columnNames) {
    StringBuilder query = new StringBuilder();
    query.append("INSERT INTO ").append(dbName).append(".").append(tableName).append("(");
    for (int i = 0; i < columnNames.length; i++) {
      query.append(columnNames[i]);
      if (i < columnNames.length - 1) {
        query.append(", ");
      }
    }
    query.append(") ");

    query.append(" VALUES ");
    for (int j = 0; j < rowsCount; j++) {
      query.append(" (");
      for (int i = 0; i < columnNames.length; i++) {
        query.append("?");
        if (i < columnNames.length - 1) {
          query.append(", ");
        }
      }
      query.append(") ");
      if (j < rowsCount - 1) {
        query.append(", ");
      }
    }

    return query.toString();
  }

  private PreparedStatement createPrepareStatement(String query, List<Map> rows)
      throws SQLException {
    PreparedStatement pst = connection.prepareStatement(query);
    int rowCounter = 0;
    String[] columnNames = (String[]) columns.keySet().toArray(new String[] {});
    for (Map row : rows) {
      Object[] rowValues = (Object[]) row.values().toArray(new Object[] {});
      for (int i = 0; i < rowValues.length; i++) {
        ColumnType type = columns.get(columnNames[i]).getType();
        Object value = rowValues[i];
        int index = rowCounter * columnNames.length + (i + 1);
        setStatementParameter(pst, index, type, value);
      }
      rowCounter++;
    }

    return pst;
  }

  private void setStatementParameter(PreparedStatement st, int valueIndex, ColumnType type,
      Object value) throws SQLException {


    if (type == null || type == ColumnType.OTHER) {
      // type is not known - nothing more we can do to narrow the type
      st.setObject(valueIndex, value);
      return;
    }


    if (type != null && type.isTimeBased() && value instanceof String) {
      value = FormatHelper.parseSqlTime(type, (String) value);
    }

    try {
      if (type == ColumnType.DATE && value instanceof Date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) value);
        st.setDate(valueIndex, new java.sql.Date(cal.getTimeInMillis()), cal);
      } else if (type == ColumnType.TIME && value instanceof Date) {
        final Time time = toTime((Date) value);
        st.setTime(valueIndex, time);
      } else if (type == ColumnType.TIMESTAMP && value instanceof Date) {
        final Timestamp ts = toTimestamp((Date) value);
        st.setTimestamp(valueIndex, ts);
      } else if (type == ColumnType.CLOB || type == ColumnType.NCLOB) {
        if (value instanceof InputStream) {
          InputStream inputStream = (InputStream) value;
          st.setAsciiStream(valueIndex, inputStream);
        } else if (value instanceof Reader) {
          Reader reader = (Reader) value;
          st.setCharacterStream(valueIndex, reader);
        } else if (value instanceof NClob) {
          NClob nclob = (NClob) value;
          st.setNClob(valueIndex, nclob);
        } else if (value instanceof Clob) {
          Clob clob = (Clob) value;
          st.setClob(valueIndex, clob);
        } else if (value instanceof String) {
          st.setString(valueIndex, (String) value);
        } else {
          st.setObject(valueIndex, value);
        }
      } else if (type == ColumnType.BLOB || type == ColumnType.BINARY) {
        if (value instanceof byte[]) {
          byte[] bytes = (byte[]) value;
          st.setBytes(valueIndex, bytes);
        } else if (value instanceof InputStream) {
          InputStream inputStream = (InputStream) value;
          st.setBinaryStream(valueIndex, inputStream);
        } else if (value instanceof Blob) {
          Blob blob = (Blob) value;
          st.setBlob(valueIndex, blob);
        } else {
          st.setObject(valueIndex, value);
        }
      } else if (type.isLiteral()) {
        final String str;
        if (value instanceof Reader) {
          Reader reader = (Reader) value;
          str = FileHelper.readAsString(reader);
        } else {
          str = value.toString();
        }
        st.setString(valueIndex, str);
      } else {
        st.setObject(valueIndex, value);
      }
    } catch (SQLException e) {
      LOGGER.error(
          "Failed to set parameter {" + valueIndex + "} to value: { " + value + "}");
      throw e;
    }
  }

  private Time toTime(Date value) {
    if (value instanceof Time) {
      return (Time) value;
    }
    final Calendar cal = Calendar.getInstance();
    cal.setTime((Date) value);
    return new java.sql.Time(cal.getTimeInMillis());
  }

  private Timestamp toTimestamp(Date value) {
    if (value instanceof Timestamp) {
      return (Timestamp) value;
    }
    final Calendar cal = Calendar.getInstance();
    cal.setTime((Date) value);
    return new Timestamp(cal.getTimeInMillis());
  }

  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory){
      this.progressIndicationFactory = progressIndicationFactory;
  }
}
