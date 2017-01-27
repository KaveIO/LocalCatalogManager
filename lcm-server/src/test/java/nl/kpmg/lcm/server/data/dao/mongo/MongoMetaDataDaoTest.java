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

package nl.kpmg.lcm.server.data.dao.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import nl.kpmg.lcm.server.LcmBaseTest;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.meatadata.MetaData;
import nl.kpmg.lcm.server.data.meatadata.MetaDataWrapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MongoMetaDataDaoTest extends LcmBaseTest {

  @Autowired
  MetaDataDao metaDataDao;

  @Test
  public void testSave() {
    String expectedName = "test";
    String expectedUri = "file://test/test";

    MetaDataWrapper metaDataWrapper = new MetaDataWrapper(new MetaData());
    metaDataWrapper.setName(expectedName);
    metaDataWrapper.setDataUri(expectedUri);

    MetaData saved = metaDataDao.save(metaDataWrapper.getMetaData());

    MetaData actual = metaDataDao.findOne(saved.getId());
    MetaDataWrapper actualMetaDataWrapper = new MetaDataWrapper(actual);
    assertFalse(actual == metaDataWrapper.getMetaData());
    assertEquals(expectedName, actualMetaDataWrapper.getName());
    assertEquals(expectedUri, actualMetaDataWrapper.getDataUri());
  }
}
