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

import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.create.ColumnCreationBuilder;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mhoekstra
 */
@BackendSource(type = "csv")
public class BackendCsvImpl extends AbstractBackend {

  /**
   * Location of the data storage on the local file system.
   *
   * @param storagePath is the directory on a local backend
   */
  private final File storagePath;

  /**
   * Default constructor.
   *
   * @param backend is {@link Storage} that contains the storagePath
   */
  public BackendCsvImpl(final Storage backend) {
    this.storagePath = new File((String) backend.getOptions().get("storagePath"));
  }

  private CsvDataContext createDataContext(final MetaData metadata) throws BackendException {
    CsvConfiguration csvConfiguration;
    Map dataOptions = metadata.getDataOptions();
    if (dataOptions != null) {
      int columnNameLine = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
      if (dataOptions.containsKey("column-name-line")) {
        columnNameLine = (int) dataOptions.get("column-name-line");
      }
      String encoding = FileHelper.DEFAULT_ENCODING;
      if (dataOptions.containsKey("encoding")) {
        encoding = (String) dataOptions.get("encoding");
      }
      char separatorChar = CsvConfiguration.DEFAULT_SEPARATOR_CHAR;
      if (dataOptions.containsKey("separator-char")) {
        separatorChar = (char) dataOptions.get("separator-char");
      }
      char quoteChar = CsvConfiguration.DEFAULT_QUOTE_CHAR;
      if (dataOptions.containsKey("quote-char")) {
        quoteChar = (char) dataOptions.get("quote-char");
      }
      char escapeChar = CsvConfiguration.DEFAULT_ESCAPE_CHAR;
      if (dataOptions.containsKey("escape-char")) {
        escapeChar = (char) dataOptions.get("escape-char");
      }
      csvConfiguration =
          new CsvConfiguration(columnNameLine, encoding, separatorChar, quoteChar, escapeChar);
    } else {
      csvConfiguration = new CsvConfiguration();
    }

    File file = getPathFromUri(metadata.getDataUri());

    return (CsvDataContext) DataContextFactory.createCsvDataContext(file, csvConfiguration);
  }


  /**
   * Returns a {@link File} specified by the URI. It checks if the URI exists and if it uses "file"
   * protocol.
   *
   * @param uri is identifier of a local file/directory
   * @return File pointed at by the URI
   * @throws BackendException if no URI is specified
   */
  private File getPathFromUri(final String uri) throws BackendException {
    URI dataUri;
    /**
     * @TODO Should we issue a warning via logger or exception in this case?
     */
    if (uri != null) {
      dataUri = parseUri(uri);

      String filePath = dataUri.getPath();

      /**
       * @TODO This is super scary. we should check if the resulting path is still within
       *       storagePath
       */

      return new File(storagePath + filePath);
    } else {
      throw new BackendException("No URI specified.");
    }
  }

  /**
   * Returns scheme supported by URI for this backend.
   *
   * @return "file" string
   */
  @Override
  protected final String getSupportedUriSchema() {
    return "csv";
  }

  /**
   * Returns information about dataset mentioned in the metadata. It checks if the referenced data
   * exist and can be accessed. It also gathers information about the size and modification time.
   *
   * @param metadata is investigated {@link MetaData} object
   * @return filled {@link DataSetInformation} object
   * @throws BackendException
   */
  @Override
  public final DataSetInformation gatherDataSetInformation(final MetaData metadata)
      throws BackendException {
    return new DataSetInformation();
  }

  /**
   * Writes an input stream to the file specified in the {@link MetaData}.
   *
   * @param metadata should contain valid destination URI
   * @param content is a stream that should be stored
   * @throws BackendException if the URI in metadata points to the existing file
   */
  public final void store(final MetaData metadata, final ContentIterator content)
      throws BackendException {

    DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
    if (dataSetInformation.isAttached()) {
      throw new BackendException("Data set is already attached, won't overwrite.");
    }


    CsvDataContext dataContext = createDataContext(metadata);

    dataContext.executeUpdate(new UpdateScript() {
      public void run(UpdateCallback callback) {
        // CREATE a table

        TableCreationBuilder createTable = callback.createTable("csv","filename");
        ColumnCreationBuilder ofType = null;
        for (Map content1 : content) {
          ofType = createTable.withColumn("name").ofType(ColumnType.STRING);
        }
        Table table = ofType.execute();

        for (Map row : content) {
          callback.insertInto(table);


          Set entrySet = row.entrySet();
          for (Object entrySet1 : entrySet) {

          }

          for (Map.Entry<String, String> entry : row.entrySet()) {

          }

              .value("id", 1).value("name", "John Doe")


              .execute();
        }
      }
    });



    for (Map row : content) {

      callback.insertInto(table).value("id", 1).value("name", "John Doe").execute();

    }



    //
    // File file = getPathFromUri(metadata.getDataUri());
    // try (FileOutputStream fos = new FileOutputStream(file)) {
    // // this works for files < 2 GB. Otherwise the copied is -1.
    // int copied = IOUtils.copy(content, fos);
    // Logger.getLogger(BackendFileImpl.class.getName())
    // .log(Level.INFO, "{0} bytes written", copied);
    // }
    // catch (IOException ex) {
    // Logger.getLogger(BackendFileImpl.class.getName())
    // .log(Level.SEVERE, "Couldn't find path: " + metadata.getDataUri(), ex);
    // }



  }

  /**
   * Writes an input stream to the file specified in the {@link MetaData}.
   *
   * @param metadata should contain valid destination URI
   * @param content is a stream that should be stored
   * @throws BackendException if the URI in metadata points to the existing file
   */
  @Override
  public final void store(final MetaData metadata, final InputStream content)
      throws BackendException {

  }

  public final void store(final MetaData metadata, final DataSet originalData)
      throws BackendException {
    store(metadata, new DataSetContentIterator(originalData));

  }


  /**
   * Returns an input stream with a content of a file that is specified by metadata argument.
   * Returns null if it is not possible to open the file. {@link MetaData} needs to contain valid
   * URI of a file.
   *
   * @param metadata MetaData with URI of the data
   * @return InputStream with the data file content
   * @throws BackendException if the metadata does not contain valid URI of a file
   */
  @Override
  public final DataSet read(final MetaData metadata) throws BackendException {
    DataContext dataContext = createDataContext(metadata);
    Schema defaultSchema = dataContext.getDefaultSchema();
    Table table = defaultSchema.getTable(0);

    return dataContext.query().from(table).selectAll().execute();
  }

  /**
   * Deletes the file specified in the {@link MetaData}.
   *
   * @param metadata {@link MetaData} with URI of the data
   * @return true if delete is successful, false otherwise
   * @throws BackendException if the metadata does not contain valid URI of a file
   */
  @Override
  public final boolean delete(final MetaData metadata) throws BackendException {
    return false;
  }
}
