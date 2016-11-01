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

package nl.kpmg.lcm.server.rest.client.version0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.RemoteLcm;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

public class RemoteLcmContollerTest extends LcmBaseServerTest {

  @Autowired
  private RemoteLcmService service;

  private static final String PATH = "client/v0/remote";
  private static final String LCM_CONTENT_TYPE
          = "application/nl.kpmg.lcm.server.data.RemoteLcm+json";
  private static final String AUTH_USER_HEADER = "LCM-Authentication-User";
  private static final Logger LOG = Logger.getLogger(RemoteLcmContollerTest.class.getName());

  /**
   * Always clean up DB.
   *
   * @throws ServerException
   */
  @After
  public void afterTest() throws ServerException {

    getWebTarget().path("client/logout").request().header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).post(null);

    List<RemoteLcm> all = service.findAll();
    for (RemoteLcm lcm : all) {
      if (lcm != null) {
        service.getDao().delete(lcm);
      }
    }
  }

  @Test
  public void testGetPost() throws ServerException {
    List<RemoteLcmRepresentation> items = getAllItems(200);
    assertEquals("Should be empty", 0, items.size());

    for (RemoteLcmRepresentation lcm : items) {
      assertNull(lcm.getItem());
    }

    RemoteLcm lcm = new RemoteLcm();
    int numOfElem = 3;
    for (int i = 0; i < numOfElem; i++) {
      lcm.setId("uid" + i);
      lcm.setUrl("http://lcm" + i);
      postLcm(lcm, 200);

      RemoteLcm retrived = getLcm("uid" + i, 200);
      assertEquals(lcm.getId(), retrived.getId());
      assertEquals(lcm.getUrl(), retrived.getUrl());
    }

    items = getAllItems(200);
    assertEquals("We should have 3 elemnts", 3, items.size());
    for (RemoteLcmRepresentation rLcm : items) {
      String uid = rLcm.getItem().getId();
      String url = rLcm.getItem().getUrl();
      boolean found = false;
      for (int i = 0; i < numOfElem; i++) {
        if (("uid" + i).equals(uid) && ("http://lcm" + i).equals(url)) {
          found = true;
          break;
        }
      }
      assertTrue("Should have found inserted IDs and URLs", found);
    }
  }

  @Test
  public void testGetPut() throws ServerException {
    List<RemoteLcmRepresentation> items = getAllItems(200);
    assertEquals("Should be empty", 0, items.size());

    for (RemoteLcmRepresentation lcm : items) {
      assertNull(lcm.getItem());
    }

    RemoteLcm lcm = new RemoteLcm();
    lcm.setId("uid" + 0);
    lcm.setUrl("http://lcm" + 0);
    putLcm(lcm, 404);

    postLcm(lcm, 200);
    //Just cheking if it's there 
    getLcm(lcm.getId(), 200);

    lcm.setUrl("http://lcm/new/path");
    putLcm(lcm, 200);
  }

  @Test
  public void testGetDelete() throws ServerException {
    List<RemoteLcmRepresentation> items = getAllItems(200);
    assertEquals("Should be empty", 0, items.size());

    for (RemoteLcmRepresentation lcm : items) {
      assertNull(lcm.getItem());
    }

    String uid = "uid" + 0;
    deleteLcm(uid, 404);

    RemoteLcm lcm = new RemoteLcm();
    lcm.setId(uid);
    lcm.setUrl("http://lcm" + 0);
    postLcm(lcm, 200);
    deleteLcm(uid, 200);
  }

  private void postLcm(RemoteLcm lcm, int expected) throws ServerException {
    Entity<RemoteLcm> entity = Entity.entity(lcm, LCM_CONTENT_TYPE);
    Response resp = getWebTarget()
            .path(PATH).request().header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).post(entity);
    assertEquals(expected, resp.getStatus());
  }

  private void putLcm(RemoteLcm lcm, int expected) throws ServerException {
    Entity<RemoteLcm> entity = Entity.entity(lcm, LCM_CONTENT_TYPE);
    Response resp = getWebTarget()
            .path(PATH)
            .path(lcm.getId())
            .request().header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).put(entity);
    assertEquals(expected, resp.getStatus());
  }

  private List<RemoteLcmRepresentation> getAllItems(int expected) throws ServerException {
    Invocation.Builder req = getWebTarget()
            .path(PATH)
            .request()
            .header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin);

    Response response = req.get();
    assertEquals(expected, response.getStatus());
    List<RemoteLcmRepresentation> items = response
            .readEntity(RemoteLcmsRepresentation.class).getItems();
    return items;
  }

  private void deleteLcm(String uid, int expected) throws ServerException {
    Response response = getWebTarget()
            .path(PATH)
            .path(uid)
            .request()
            .header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).delete();

    assertEquals(expected, response.getStatus());

  }

  private RemoteLcm getLcm(String uid, int expected) throws ServerException {
    Response response = getWebTarget()
            .path(PATH)
            .path(uid)
            .request()
            .header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(expected, response.getStatus());
    return response
            .readEntity(RemoteLcmRepresentation.class).getItem();
  }

}
