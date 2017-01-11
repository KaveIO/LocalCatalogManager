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
import java.util.Map;


/***
 * The class contains unit tests for BackendHiveImpl
 * 
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class BackendHiveImplTest {

  private static final String TEST_STORAGE_PATH = "temp_test/";
  private static final String TEST_BACKEND_NAME = "test";

  /**
   * Common access tool for all backends.
   */
  private Storage backendStorage;

  /**
   * Default constructor.
   */
  public BackendHiveImplTest() {    
    backendStorage = new Storage();
    backendStorage.setName("hive-sotrage");
    Map options = new HashMap();
    options.put("username", "hive");
    options.put("password", "hive");
    options.put("database", "default");
    options.put("url", "jdbc:hive2://10.191.30.201:10000");
    options.put("driver", "org.apache.hive.jdbc.HiveDriver");
    backendStorage.setOptions(options);
  }

  /**
   * Test to check if "hive" URI scheme is supported by getSupportedUriSchema() method.
   */
  @Test
  public final void testGetSupportedUriSchema() {
    String uri = "hive://remote-hive-foodmart/product";
    MetaData metaData = new MetaData();
    metaData.setDataUri(uri);
    
    BackendHiveImpl testBackend = new BackendHiveImpl(backendStorage, metaData);
    String testSchema = testBackend.getSupportedUriSchema();
    assertEquals("hive", testSchema);
  }

  /**
   * Tests if the URI is parsed correctly in {@link BackendFileImpl} class.
   *
   * @throws BackendException if it is not possible to parse the URI
   */
  @Test
  public final void testParseUri() {
   String uri = "hive://remote-hive-foodmart/product";
    MetaData metaData = new MetaData();
    metaData.setDataUri(uri);
    
    BackendHiveImpl testBackend = new BackendHiveImpl(backendStorage, metaData);
    URI dataUri = testBackend.getDataUri();

    assertEquals("hive", dataUri.getScheme());
    assertEquals("remote-hive-foodmart", dataUri.getHost());
    assertEquals("/product", dataUri.getPath());
  }
}
