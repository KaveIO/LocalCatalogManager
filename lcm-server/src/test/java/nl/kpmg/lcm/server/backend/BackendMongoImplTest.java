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
import static org.mockito.Mockito.mock;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.test.mock.MetaDataMocker;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.Test;

import java.util.Set;

/***
 * The class contains unit tests for BackendMongoImpl
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class BackendMongoImplTest {
  /**
   * Test to check if "hive" URI scheme is supported by getSupportedUriSchema() method.
   */
  private StorageService storageService = mock(StorageService.class);

  @Test
  public final void testGetSupportedUriSchema() {
    MetaDataWrapper metaDataWrapper = MetaDataMocker.getMongoMetaDataWrapper();
    Storage backendStorage = StorageMocker.createMongoStorage();
    BackendMongoImpl testBackend = new BackendMongoImpl(metaDataWrapper.getMetaData(),  storageService);
    Set<String> testSchema = testBackend.getSupportedUriSchema();
    assertTrue(testSchema.contains(DataFormat.MONGO));
  }
}
