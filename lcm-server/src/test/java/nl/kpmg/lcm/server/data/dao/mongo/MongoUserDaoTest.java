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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.LcmBaseTest;
import nl.kpmg.lcm.server.data.dao.UserDao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MongoUserDaoTest extends LcmBaseTest {

  @Autowired
  UserDao userDao;

  @Test
  public void testSaveHashesPassword() throws UserPasswordHashException {
    User expected = new User();
    expected.setName("testUser");
    expected.setPassword("testPassword");
    expected.setOrigin(User.LOCAL_ORIGIN);

    assertFalse(expected.isHashed());
    userDao.save(expected);

    User actual = userDao.findOneByNameAndOrigin("testUser", User.LOCAL_ORIGIN);
    assertTrue(actual.isHashed());
    assertFalse(actual.getPassword().equals("testPassword"));
    assertTrue(actual.passwordEquals("testPassword"));
  }
}
