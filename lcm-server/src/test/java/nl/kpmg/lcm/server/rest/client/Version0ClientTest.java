package nl.kpmg.lcm.server.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Test;

import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

public class Version0ClientTest extends LCMBaseServerTest {

  @Test
  public void testGetIndex() throws LoginException, IOException, ServerException {
    Response response = getWebTarget().path("client/v0").request()
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(200, response.getStatus());

    String restult = response.readEntity(String.class);
    ObjectMapper objectMapper = new ObjectMapper();
    Map responseMap = objectMapper.readValue(restult, Map.class);

    assertTrue(responseMap.containsKey("links"));
    List responseLinkList = (List) responseMap.get("links");
    assertEquals(7, responseLinkList.size());
  }
}
