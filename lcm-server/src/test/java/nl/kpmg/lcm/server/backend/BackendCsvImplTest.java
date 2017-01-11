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

import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;

import org.junit.Test;

import java.net.URI;
import java.util.HashMap;


/***
 * The class contains unit tests for BackendCsvImpl
 * 
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class BackendCsvImplTest {

  private static final String TEST_STORAGE_PATH = "temp_test/";
  private static final String TEST_BACKEND_NAME = "test";

  /**
   * Common access tool for all backends.
   */
  private Storage backendStorage;

  /**
   * Default constructor.
   */
  public BackendCsvImplTest() {
    backendStorage = new Storage();
    backendStorage.setId(TEST_BACKEND_NAME);
    backendStorage.setOptions(new HashMap());
    backendStorage.getOptions().put("storagePath", TEST_STORAGE_PATH);

  }

  /**
   * Test to check if "csv" URI scheme is supported by getSupportedUriSchema() method.
   */
  @Test
  public final void testGetSupportedUriSchema() {
    String uri = "csv://test/temp.csv";
    MetaData metaData = new MetaData();
    metaData.setDataUri(uri);
    BackendCsvImpl testBackend = new BackendCsvImpl(backendStorage, metaData);
    String testSchema = testBackend.getSupportedUriSchema();
    assertEquals("csv", testSchema);
  }

  /**
   * Tests if the URI is parsed correctly in {@link BackendFileImpl} class.
   *
   * @throws BackendException if it is not possible to parse the URI
   */
  @Test
  public final void testParseUri() {
    String uri = "csv://test/temp.csv";
    MetaData metaData = new MetaData();
    metaData.setDataUri(uri);

    BackendCsvImpl testBackend = new BackendCsvImpl(backendStorage, metaData);
    URI dataUri = testBackend.getDataUri();

    assertEquals("csv", dataUri.getScheme());
    assertEquals("test", dataUri.getHost());
    assertEquals("/temp.csv", dataUri.getPath());
  }
}
