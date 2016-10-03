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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;

import org.apache.commons.io.IOUtils;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackendCsvImplTest {

  /**
   * Temporary directory in which all the test files will exist.
   */
  private static final String TEST_STORAGE_PATH = "temp_test/";
  private static final String TEST_STORAGE_FILE = "testFile.csv";


  private static final String TEST_BACKEND_NAME = "test";
  private static final String TEST_BACKEND_FILE_URI =
      "csv://" + TEST_BACKEND_NAME + "/" + TEST_STORAGE_FILE;

  /**
   * Common access tool for all backends.
   */
  private final Storage backendModel;

  /**
   * Default constructor.
   */
  public BackendCsvImplTest() {
    backendModel = new Storage();
    backendModel.setId(TEST_BACKEND_NAME);
    backendModel.setOptions(new HashMap());
    backendModel.getOptions().put("storagePath", TEST_STORAGE_PATH);
  }

  /**
   * Makes a temporary test directory.
   *
   * @throws Exception if it is not possible to make a test directory.
   */
  @BeforeClass
  public static final void setUpClass() throws Exception {
    // make test temp dir and set storage path
    File testDir = new File(TEST_STORAGE_PATH);
    testDir.mkdir();

    // Make a test file with some content
    File testFile = new File(TEST_STORAGE_PATH + TEST_STORAGE_FILE);
    testFile.createNewFile();
    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write("column_a,column_b,column_c\n");
      writer.write("value_a,123,c\n");
      writer.flush();
    }
  }

  /**
   * Deletes the temporary test directory and its content, assuming there are no subdirectories.
   */
  @AfterClass
  public static final void tearDownClass() {
    File file = new File(TEST_STORAGE_PATH);
    for (File c : file.listFiles()) {
      c.delete();
    }
    file.delete();
  }

  /**
   * Test to check if "file" URI scheme is supported by getSupportedUriSchema() method.
   */
  @Test
  public final void testGetSupportedUriSchema() {
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    String testSchema = testBackend.getSupportedUriSchema();
    assertEquals("csv", testSchema);
  }

  /**
   * Tests if the URI is parsed correctly in {@link BackendFileImpl} class.
   *
   * @throws BackendException if it is not possible to parse the URI
   * @throws IOException if it is not possible to get full canonical path of a storage location.
   */
  @Test
  public final void testParseUri() throws BackendException, IOException {
    File testDir = new File(TEST_STORAGE_PATH);
    String uri = "csv://test/temp.csv";

    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    URI dataUri = testBackend.parseUri(uri);

    assertEquals("csv", dataUri.getScheme());
    assertEquals("test", dataUri.getHost());
    assertEquals("/temp.csv", dataUri.getPath());
  }

  /**
   * Tests what happens if {@link BackendFileImp} gathers information using empty {@link MetaData}
   * object. Exception is expected.
   *
   * @throws BackendException if empty metadata are supplied.
   */
  @Test(expected = BackendException.class)
  public final void testGatherDatasetInformationEmptyMetadata() throws BackendException {
    MetaData metaData = new MetaData();
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
  }

  /**
   * Tests what happens if {@link BackendFileImp} gathers information using {@link MetaData} object
   * with invalid URI. Exception is expected.
   *
   * @throws BackendException if invalid URI is supplied in metadata
   */
  @Test(expected = BackendException.class)
  public final void testGatherDatasetInformationWrongMetadata() throws BackendException {
    MetaData metaData = new MetaData();
    final String fileUri = "NotAnUri";
    metaData.setDataUri(fileUri);
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
  }

  /**
   * Tests what happens if {@link BackendFileImp} gathers information using {@link MetaData} object
   * with valid URI pointing to non-existing location. The {@link DataSetInformation} object should
   * has isAttached() method equal to false.
   *
   * @throws BackendException if it is not possible to gather information about the dataset
   * @throws IOException if it is not possible to get path of the storage directory
   */
  @Test
  public final void testGatherDatasetInformationWrongLink() throws BackendException, IOException {
    MetaData metaData = new MetaData();
    File testDir = new File(TEST_STORAGE_PATH);
    // need to make sure that test file does not exist
    File testFile = new File(TEST_STORAGE_PATH + "/temp.csv");
    testFile.delete();

    metaData.setDataUri("csv://test/temp.csv");
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
    assertEquals(dataSetInformation.isAttached(), false);
  }

  /**
   * Tests what happens if {@link BackendFileImp} gathers information using {@link MetaData} object
   * with valid URI pointing to existing location. The {@link DataSetInformation} object should has
   * isAttached() method equal to true.
   *
   * @throws BackendException if it is not possible to gather information about the dataset
   * @throws IOException if it is not possible to get path of the storage directory
   */
  @Test
  public final void testGatherDatasetInformation() throws BackendException, IOException {
    MetaData metaData = new MetaData();
    File testDir = new File(TEST_STORAGE_PATH);
    File testFile = new File(TEST_STORAGE_PATH + "/temp.csv");
    if (!testFile.exists()) {
      new FileOutputStream(testFile).close();
    }
    Date expTimestamp = new Date(testFile.lastModified());
    metaData.setDataUri("csv://test/temp.csv");
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    DataSetInformation dataSetInformation = testBackend.gatherDataSetInformation(metaData);
    assertEquals(dataSetInformation.getModificationTime(), expTimestamp);
  }

  /**
   * Tests store() method of {@link BackendFileImp}. Test creates a text file, then tries to store
   * it and finally tests if it is identical using md5.
   *
   * @throws BackendException if it is not possible to gather information about the dataset
   * @throws IOException if it is not possible to get path of the storage directory
   */
  @Ignore
  @Test
  public final void testStore() throws IOException, BackendException {

    // now make a metadata with uri
    File testDir = new File(TEST_STORAGE_PATH);
    final String fileUri = "csv://test/testStore.csv";


    MetaData originalMetaData = new MetaData();
    originalMetaData.setDataUri(TEST_BACKEND_FILE_URI);

    MetaData newMetaData = new MetaData();
    newMetaData.setDataUri(fileUri);

    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    DataSet originalData = testBackend.read(originalMetaData);
    testBackend.store(newMetaData, originalData);
  }

  /**
   * Tests store() method of {@link BackendFileImp}. Test creates a text file, then tries to store
   * it and modify it afterwards. Finally tests if files are different using md5.
   *
   * @throws BackendException if it is not possible to gather information about the dataset
   * @throws IOException if it is not possible to get path of the storage directory
   */
  @Ignore
  @Test
  public final void testStore2() throws IOException, BackendException {
    // first make a test file with some content
    File testFile = new File(TEST_STORAGE_PATH + "/testFile2.csv");
    testFile.createNewFile();
    try (FileWriter writer = new FileWriter(testFile)) {
      final int nLoops = 10;
      for (int i = 0; i < nLoops; i++) {
        writer.write("qwertyuiop");
        writer.write("\n");
        writer.write("asdfghjkl");
        writer.write("\n");
        writer.write("zxcvbnm,!@#$%^&*()_");
        writer.write("\n");
        writer.write("1234567890[][;',.");
        writer.write("\n");
      }
      writer.flush();
    }
    // now make a metadata with uri
    File testDir = new File(TEST_STORAGE_PATH);
    final String fileUri = "file://" + testDir.getCanonicalPath() + "/testStore2.csv";
    MetaData metaData = new MetaData();
    metaData.setDataUri(fileUri);
    InputStream is = new FileInputStream(testFile);
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    testBackend.store(metaData, is);
    try (FileWriter writer = new FileWriter(testFile, true)) {
      writer.write("\n");
      writer.flush();
    }
    final File expected = testFile;
    final File output = new File(testDir.getCanonicalPath() + "/testStore2.csv");
    HashCode hcExp = Files.hash(expected, Hashing.md5());
    HashCode hcOut = Files.hash(output, Hashing.md5());
    assertNotSame(hcExp.toString(), hcOut.toString());
  }

  /**
   * Tests read() method of {@link BackendFileImp}. Test reads a text file created by previous test
   * and stores in the new text file. Then it tests if the 2 files are identical using md5.
   *
   * @throws java.io.IOException if the canonical path of the storage location cannot be resolved
   * @throws BackendException if it is not possible to read from the test backend
   */
  @Test
  public final void testRead() throws IOException, BackendException {
    // make a metadata with uri
    MetaData metaData = new MetaData();
    metaData.setDataUri(TEST_BACKEND_FILE_URI);

    // make local data backend in specified directory and read the existing file
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    DataSet dataSet = testBackend.read(metaData);

    for (Row row : dataSet) {
      assertEquals("value_a", row.getValue(0));
      assertEquals("123", row.getValue(1));
      assertEquals("c", row.getValue(2));
    }
  }

  /**
   * Tests delete() method of {@link BackendFileImp}. It tries to delete one of the files created by
   * previous tests. It fails if it is not possible to delete the file.
   *
   * @throws IOException if the canonical path of the storage location cannot be resolved
   * @throws BackendException if it is not possible to delete on the test backend
   */
  @Ignore
  @Test
  public final void testDelete() throws IOException, BackendException {
    File testDir = new File(TEST_STORAGE_PATH);
    File testFile = new File(TEST_STORAGE_PATH + "/testDelete.csv");
    testFile.createNewFile();
    try (FileWriter writer = new FileWriter(testFile)) {
      final int nLoops = 10;
      for (int i = 0; i < nLoops; i++) {
        writer.write("qwertyuiop");
        writer.write("\n");
        writer.write("asdfghjkl");
        writer.write("\n");
        writer.write("zxcvbnm,!@#$%^&*()_");
        writer.write("\n");
        writer.write("1234567890[][;',.");
        writer.write("\n");
      }
      writer.flush();
    }
    final String fileUri = "file://" + testDir.getCanonicalPath() + "/testDelete.csv";
    // make metadata pointing to the file to be deleted
    MetaData metaData = new MetaData();
    metaData.setDataUri(fileUri);
    // make local data backend in specified directory and delete the existing file
    BackendCsvImpl testBackend = new BackendCsvImpl(backendModel);
    boolean result = testBackend.delete(metaData);
    assertEquals(result, true);
  }
}

