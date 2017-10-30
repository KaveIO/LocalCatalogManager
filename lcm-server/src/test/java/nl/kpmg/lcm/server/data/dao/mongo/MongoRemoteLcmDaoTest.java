/*
  * Copyright 2017 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.common.rest.authentication.PasswordHash;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.LcmBaseTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Test if converters work correct i.e.  the same object is read and written
 * This is needed because we overwrite the default mongo reader/writer
 * @author shristov
 */
public class MongoRemoteLcmDaoTest extends LcmBaseTest {

  @Autowired
  MongoAuthorizedLcmDao dao;

  @Test
  public void testReadWrite() throws UserPasswordHashException {
    AuthorizedLcm originalLcm = new AuthorizedLcm();
    originalLcm.setApplicationId("applicationId");
    String hashedKey = PasswordHash.createHash("applicationKey");
    originalLcm.setApplicationKey(hashedKey);
    
    originalLcm.setName("name");
    originalLcm.setUniqueId("uniqueId");

    originalLcm = dao.save(originalLcm);
    AuthorizedLcm retreviedLcm = dao.findOneById(originalLcm.getId());
  
    assertEquals(retreviedLcm.getApplicationId(), originalLcm.getApplicationId());
    assertEquals(retreviedLcm.getApplicationKey(), originalLcm.getApplicationKey());
    assertEquals(retreviedLcm.getName(), originalLcm.getName());
    assertEquals(retreviedLcm.getUniqueId(), originalLcm.getUniqueId());
  }
}
