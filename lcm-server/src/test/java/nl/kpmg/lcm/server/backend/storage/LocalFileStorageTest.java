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

package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.exception.LcmValidationException;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class LocalFileStorageTest {

  @Test
  public void testCreateStorage() {
    Storage correctStorage = new Storage();
    correctStorage.setName("csv-sotrage");
    Map options = new HashMap();
    options.put("storagePath", "/tmp");
    correctStorage.setType(DataFormat.CSV);
    correctStorage.setOptions(options);

    LocalFileStorage hiveStorage = new LocalFileStorage(correctStorage);
    Assert.assertNotNull(hiveStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingStoragePath() {
    Storage incrrectStorage = new Storage();
    incrrectStorage.setName("csv-sotrage");
    Map options = new HashMap();
    incrrectStorage.setOptions(options);

    new LocalFileStorage(incrrectStorage);
  }
}
