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

import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.test.mock.MetaDataMocker;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.Test;

import java.util.Set;


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
    backendStorage = StorageMocker.createCsvStorage();
  }

  /**
   * Test to check if "csv" URI scheme is supported by getSupportedUriSchema() method.
   */
  @Test
  public final void testGetSupportedUriSchema() {
    MetaDataWrapper metaDataWrapper = MetaDataMocker.getCsvMetaDataWrapper();
    BackendCsvImpl testBackend = new BackendCsvImpl(backendStorage, metaDataWrapper.getMetaData());
    Set<String> testSchema = testBackend.getSupportedUriSchema();
    assertTrue(testSchema.contains(DataFormat.CSV));
  }
}
