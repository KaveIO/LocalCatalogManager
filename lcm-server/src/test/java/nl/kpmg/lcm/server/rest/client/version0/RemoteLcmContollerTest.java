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

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.BASIC_AUTHENTICATION_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.common.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.test.mock.RemoteLcmMocker;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public class RemoteLcmContollerTest extends LcmBaseServerTest {

  @Autowired
  private RemoteLcmService service;

  private static final String PATH = "client/v0/remoteLcm";
  private static final String LCM_CONTENT_TYPE
          = "application/nl.kpmg.lcm.server.data.RemoteLcm+json";
  private static final String AUTH_USER_HEADER = "LCM-Authentication-User";

  /**
   * Always clean up DB.
   *
   * @throws ServerException
   */
  @After
  public void afterTest() throws ServerException {

    getWebTarget().path("client/logout").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).post(null);

    List<RemoteLcm> all = service.findAll();
    for (RemoteLcm lcm : all) {
      if (lcm != null) {
        service.delete(lcm.getId());
      }
    }
  }


 @Test
  public void testGet() throws ServerException {
    List<RemoteLcmRepresentation> items = getAllItems(200);
    assertEquals("Should be empty", 0, items.size());

    for (RemoteLcmRepresentation lcm : items) {
      assertNull(lcm.getItem());
    }

    
    int numOfElem = 3;
    Map<String, RemoteLcm> map = new HashMap<String, RemoteLcm>();
    for (int i = 0; i < numOfElem; i++) {
      RemoteLcm lcm = RemoteLcmMocker.createRemoteLcm();
      service.create(lcm);

      RemoteLcm retrived = getLcm(lcm.getId(), 200);
      assertEquals(lcm.getId(), retrived.getId());
      assertEquals(getUrl(lcm), getUrl(retrived));

      map.put(lcm.getId(), lcm);
    }

    items = getAllItems(200);
    assertEquals("We should have 3 elemnts", 3, items.size());
    for (RemoteLcmRepresentation rLcm : items) {
      String uid = rLcm.getItem().getId();
      String url = getUrl(rLcm.getItem());
      boolean found = false;
      if (map.get(uid) != null && getUrl(map.get(uid)).equals(url)) {
        found = true;
        break;
      }
      assertTrue("Should have found inserted IDs and URLs", found);
    }
  }

  private String getUrl(RemoteLcm lcm)   {
    String url = String.format("%s://%s", lcm.getProtocol(), lcm.getDomain());
    if(lcm.getPort() !=  null) {
       url += ":" + lcm.getPort() ;
    }
    return url;
  }

  @Test
  public void testPutPost() throws ServerException {
    List<RemoteLcmRepresentation> items = getAllItems(200);
    assertEquals("Should be empty", 0, items.size());

    for (RemoteLcmRepresentation lcm : items) {
      assertNull(lcm.getItem());
    }

    RemoteLcm lcm = new RemoteLcm();
    putLcm(lcm, 400);

    lcm = RemoteLcmMocker.createRemoteLcm();
    postLcm(lcm, 200);
    //Just cheking if it's there 
    RemoteLcm  read = service.findOneById(lcm.getId());
    Assert.assertNotNull(read);
    lcm.setProtocol("http");
    lcm.setDomain("lcm/new/path");
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

    RemoteLcm lcm = RemoteLcmMocker.createRemoteLcm();
    service.create(lcm);
    deleteLcm(lcm.getId(), 200);
  }

  private void postLcm(RemoteLcm lcm, int expected) throws ServerException {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.put("name", lcm.getName());
      rootNode.put("uniqueId", lcm.getUniqueId());
      rootNode.put("applicationId", lcm.getApplicationId());
      rootNode.put("applicationKey", lcm.getApplicationKey());
      rootNode.put("protocol", lcm.getProtocol());
      rootNode.put("domain", lcm.getDomain());
      rootNode.put("port", lcm.getPort()); 
      rootNode.put("status", lcm.getStatus());
      rootNode.put("id", lcm.getId());

    Entity<String> entity = Entity.entity(rootNode.toString(), LCM_CONTENT_TYPE);
    Response resp = getWebTarget()
            .path(PATH).request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).post(entity);
    assertEquals(expected, resp.getStatus());
  }

  private void putLcm(RemoteLcm lcm, int expected) throws ServerException {
    Entity<RemoteLcm> entity = Entity.entity(lcm, LCM_CONTENT_TYPE);
    Response resp = getWebTarget()
            .path(PATH)
            .request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).put(entity);
    assertEquals(expected, resp.getStatus());
  }

  private List<RemoteLcmRepresentation> getAllItems(int expected) throws ServerException {
    Invocation.Builder req = getWebTarget()
            .path(PATH)
            .request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin);

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
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).delete();

    assertEquals(expected, response.getStatus());

  }

  private RemoteLcm getLcm(String uid, int expected) throws ServerException {
    Response response = getWebTarget()
            .path(PATH)
            .path(uid)
            .request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(expected, response.getStatus());
    return response
            .readEntity(RemoteLcmRepresentation.class).getItem();
  }

}
