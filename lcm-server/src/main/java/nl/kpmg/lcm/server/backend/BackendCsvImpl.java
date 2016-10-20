/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.server.data.ContentIterator;
import java.io.File;
import java.io.IOException;
import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.data.MetaData;
import org.apache.metamodel.data.DataSet;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.backend.exception.BadMetaDataException;
import nl.kpmg.lcm.server.backend.exception.DataSourceValidationException;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.Storage;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = "csv")
public class BackendCsvImpl extends AbstractBackend {

  private static final Logger logger = Logger.getLogger(BackendCsvImpl.class.getName());
  private File dataSourceFile = null;
  
  /**
   *
   * @param backendStorage valid storage. This storage name must be extracted from @metaData object
   *        and then the storage object loaded be loaded .
   * @param metaData - valid @metaData that representing CSV data source.
   * @throws BadMetaDataException when the @metaData is null or it is not consistent.
   * @throws nl.kpmg.lcm.server.backend.exception.DataSourceValidationException
   */
  public BackendCsvImpl(Storage backendStorage, MetaData metaData)
      throws DataSourceValidationException, BackendException {
    super(backendStorage, metaData);
    String storagePath = (String) backendStorage.getOptions().get("storagePath");
    dataSourceFile = createDataSourceFile(storagePath);
  }

  @Override
  protected String getSupportedUriSchema() {
    return "csv";
  }

  private UpdateableDataContext createDataContext() throws BackendException {
    if (metaData == null) {
      throw new IllegalStateException("MetaData parameter could not be null");
    }

    CsvConfiguration csvConfiguration = getConfiguration();

    if (!dataSourceFile.exists()) {
      throw new DataSourceValidationException("Unable to find data source file! FilePath"
          + dataSourceFile.getPath());
    }
    return (CsvDataContext) DataContextFactory.createCsvDataContext(dataSourceFile,
        csvConfiguration);
  }

  private CsvConfiguration getConfiguration() {
    // TODO these options must be dynamically loaded from metada opject
    // However, until the metadata is not refactored they will be in this way.
    int columnNameLine = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
    String encoding = FileHelper.DEFAULT_ENCODING;
    char separatorChar = CsvConfiguration.DEFAULT_SEPARATOR_CHAR;
    char quoteChar = CsvConfiguration.DEFAULT_QUOTE_CHAR;
    char escapeChar = CsvConfiguration.DEFAULT_ESCAPE_CHAR;

    // !TODO
    // When you refactoring MetaData and objects around it
    // keep in mind that validation must be done to all imput data
    // for example this is valid scenario
    // "column-name-line": "kdlfjhsadjkfh"
    Map dataOptions = metaData.getDataOptions();
    if (dataOptions != null) {
      columnNameLine = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
      if (dataOptions.containsKey("column-name-line")) {
        columnNameLine = (int) dataOptions.get("column-name-line");
      }
      if (dataOptions.containsKey("encoding")) {
        encoding = (String) dataOptions.get("encoding");
      }
      if (dataOptions.containsKey("separator-char")) {
        separatorChar = (char) dataOptions.get("separator-char");
      }
      if (dataOptions.containsKey("quote-char")) {
        quoteChar = (char) dataOptions.get("quote-char");
      }
      if (dataOptions.containsKey("escape-char")) {
        escapeChar = (char) dataOptions.get("escape-char");
      }
    }

    CsvConfiguration csvConfiguration =
        new CsvConfiguration(columnNameLine, encoding, separatorChar, quoteChar, escapeChar);
    return csvConfiguration;
  }

  private File createDataSourceFile(String storagePath) throws DataSourceValidationException {
    if (dataSourceFile != null) {
      return dataSourceFile;
    }

    File baseDir = new File(storagePath);
    URI dataUri = getDataUri();

    String filePath = dataUri.getPath();
    dataSourceFile = new File(storagePath + filePath);

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, dataSourceFile, notification);

    if (notification.hasErrors()) {
      throw new DataSourceValidationException(notification.errorMessage());
    }

    return dataSourceFile;
  }

 



  @Override
  public DataSetInformation gatherDataSetInformation() throws BackendException {
    DataSetInformation info = new DataSetInformation();
    try {
      info.setUri(dataSourceFile.getCanonicalPath());
      info.setAttached(dataSourceFile.exists());
      if (dataSourceFile.exists()) {

        info.setByteSize(dataSourceFile.length());
        info.setModificationTime(new Date(dataSourceFile.lastModified()));
      }
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Unable to get info about datasource: " + dataSourceFile.getPath(),
          ex);
      throw new BackendException(ex);
    }

    return info;
  }

  /**
   * Method to store some content on a data storage backend.
   *
   * @param content {@link ContentIterator} that should be stored.
   * @param forceOverwrite - indicates how to proceed if the content already exists - in case of
   *        true the content is written no matter if already persists or not - in case it is set to
   *        false then the content is written only when it doesn't exist - in case it is set to
   *        false and the content already exists then BackendExceptionis thrown
   * @throws BackendException if - the URI is not valid or it is not possible to reach the storage.
   *         - @forceUpdateIfExists is false and the content already exists.
   */
  @Override
  public void store(ContentIterator content, boolean forceOverwrite) throws BackendException {
    DataSetInformation dataSetInformation = gatherDataSetInformation();
    if (dataSetInformation.isAttached() && !forceOverwrite) {
      throw new BackendException("Data set is already attached, won't overwrite.");
    }

    try (Writer writer = FileHelper.getBufferedWriter(dataSourceFile);) {

      CsvConfiguration configuration = getConfiguration();
      CsvWriter csvWriter = new CsvWriter(configuration);
      int rowNumber = 1;
      while (content.hasNext()) {

        Map row = content.next();

        if (rowNumber == configuration.getColumnNameLineNumber()) {
          String[] columnLineInParts = (String[]) row.keySet().toArray(new String[] {});
          String columnLine = csvWriter.buildLine(columnLineInParts);
          writer.write(columnLine);
          rowNumber++;
        }

        String[] lineInParts = (String[]) row.values().toArray(new String[] {});
        String line = csvWriter.buildLine(lineInParts);
        writer.write(line);
        rowNumber++;
      }

      writer.flush();
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error occured during saving information!", ex);
    }
  }

  /**
   * Method to read some content from a data storage backend.
   *
   * @return {@link DataSet} with all the data specified in the @metaData object passed during
   *         initialization.
   * @throws BackendException if the URI is not valid or it is not possible to reach the storage.
   */
  @Override
  public Data read() throws BackendException {
    UpdateableDataContext dataContext = createDataContext();
    Schema schema = dataContext.getDefaultSchema();
    if (schema.getTableCount() == 0) {
      return null;
    }
    Table table = schema.getTables()[0];
    DataSet result = dataContext.query().from(table).selectAll().execute();

    return new Data(metaData, new DataSetContentIterator(result));
  }

  @Override
  public boolean delete() throws BackendException {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

  @Override
  protected void extraValidation(Storage backendStorage, MetaData metaData,
      Notification notification) {

    String storagePath = (String) backendStorage.getOptions().get("storagePath");
    if (storagePath == null || storagePath.isEmpty()) {
      notification.addError("Storage path missing or is empty!", null);
    }
  }

}
