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

import nl.kpmg.lcm.server.backend.metadata.CsvMetaData;
import nl.kpmg.lcm.server.backend.storage.CsvStorage;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = "csv")
public class BackendCsvImpl extends AbstractBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendCsvImpl.class.getName());
  private File dataSourceFile = null;
  private final CsvMetaData csvMetaData;

  /**
   *
   * @param backendStorage valid storage. This storage name must be extracted from @metaData object
   *        and then the storage object loaded be loaded .
   * @param metaData - valid @metaData that representing CSV data source.
   */
  public BackendCsvImpl(Storage backendStorage, MetaData metaData) {
    super(metaData);
    String storagePath = new CsvStorage(backendStorage).getStoragePath();
    dataSourceFile = createDataSourceFile(storagePath);
    this.csvMetaData = new CsvMetaData(metaData);
  }

  @Override
  protected String getSupportedUriSchema() {
    return "csv";
  }

  private UpdateableDataContext createDataContext() {
    if (csvMetaData == null) {
      throw new IllegalStateException("MetaData parameter could not be null");
    }

    CsvConfiguration csvConfiguration = csvMetaData.getConfiguration();

    if (!dataSourceFile.exists()) {
      throw new LcmException("Unable to find data source file! FilePath: " + dataSourceFile.getPath());
    }
    return (CsvDataContext) DataContextFactory.createCsvDataContext(dataSourceFile,
        csvConfiguration);
  }


  private File createDataSourceFile(String storagePath) {
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
      throw new LcmValidationException(notification);
    }

    return dataSourceFile;
  }

  @Override
  public DataSetInformation gatherDataSetInformation() {
    DataSetInformation info = new DataSetInformation();
    try {
      info.setUri(dataSourceFile.getCanonicalPath());
      info.setAttached(dataSourceFile.exists());
      if (dataSourceFile.exists()) {

        info.setByteSize(dataSourceFile.length());
        info.setModificationTime(new Date(dataSourceFile.lastModified()));
      }
    } catch (IOException ex) {
      LOGGER.error("Unable to get info about datasource: " + dataSourceFile.getPath(), ex);
      throw new LcmException("Unable to get info about datasource: " + dataSourceFile.getPath(), ex);
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
   *        false and the content already exists then LcmException is thrown.
   */
  @Override
  public void store(ContentIterator content, DataTransformationSettings transformationSettings,
      boolean forceOverwrite) {
    DataSetInformation dataSetInformation = gatherDataSetInformation();
    if (dataSetInformation.isAttached() && !forceOverwrite) {
      throw new LcmException("Data set is already attached, won't overwrite.");
    }

    int rowNumber = 1;
    try (Writer writer = FileHelper.getBufferedWriter(dataSourceFile);) {

      CsvConfiguration configuration = csvMetaData.getConfiguration();
      CsvWriter csvWriter = new CsvWriter(configuration);

      if (progressIndicationFactory != null) {
        String message = "Start transfer.";
        progressIndicationFactory.writeIndication(message);
      }
      while (content.hasNext()) {

        Map row = content.next();

        if (rowNumber == configuration.getColumnNameLineNumber()) {
          Object[] lineAsObjectValues = (Object[]) row.keySet().toArray(new Object[] {});
          String[] lineAsStringValues = toStringArray(lineAsObjectValues);
          String columnLine = csvWriter.buildLine(lineAsStringValues);
          writer.write(columnLine);
          rowNumber++;
        }

        Object[] lineAsObjectValues = (Object[]) row.values().toArray(new Object[] {});
        String[] lineAsStringValues = toStringArray(lineAsObjectValues);
        String line = csvWriter.buildLine(lineAsStringValues);
        writer.write(line);
        rowNumber++;
        if(progressIndicationFactory != null
                && rowNumber % progressIndicationFactory.getIndicationChunkSize() == 0) {
            String message =  "Written " + (rowNumber -1) +  " records!";
            progressIndicationFactory.writeIndication(message);
        }
      }
      writer.flush();
      String message = "Written successfully all the records: " + (rowNumber -1);
      if(progressIndicationFactory != null) {
        progressIndicationFactory.writeIndication(message);
      }
    } catch (IOException ex) {
      LOGGER.error("Error occured during saving information!", ex);
      if (progressIndicationFactory != null) {
        String message =
            String.format("The content is inserted partially, only %d rows in file: %s",
                (rowNumber -1), dataSourceFile.getName());
        progressIndicationFactory.writeIndication(message);
      }
    }
  }

  private String[] toStringArray(Object[] lineAsObjectValues) {
    String[] lineAsStringValues = new String[lineAsObjectValues.length];
    for (int i = 0; i < lineAsObjectValues.length; i++) {
      lineAsStringValues[i] = lineAsObjectValues[i].toString();
    }
    return lineAsStringValues;
  }

  /**
   * Method to read some content from a data storage backend.
   *
   * @return {@link DataSet} with all the data specified in the @metaData object passed during
   *         initialization.
   */
  @Override
  public Data read() {
    UpdateableDataContext dataContext = createDataContext();
    Schema schema = dataContext.getDefaultSchema();
    if (schema.getTableCount() == 0) {
      return null;
    }
    Table table = schema.getTables()[0];
    DataSet result = dataContext.query().from(table).selectAll().execute();
    csvMetaData.getTableDescription().setColumns(table.getColumns());
    return new Data(csvMetaData.getMetaData(), new DataSetContentIterator(result));
  }

  @Override
  public boolean delete() {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

  @Override
  public void free() {

  }

  @Override
  protected void extraValidation(MetaDataWrapper metaDataWrapper, Notification notification) {

  }

}
