/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package nl.kpmg.lcm.server.integration.atlas;

import nl.kpmg.lcm.common.client.HttpClientFactory;
import nl.kpmg.lcm.common.configuration.AtlasConfiguration;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.exception.LcmException;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author shristov
 */
@Service
public class AtlasRequestProxy {
  String API_URL = "api/atlas/v2/";

  @Autowired
  private AtlasConfiguration atlasConfiguration;

  private Response executeGetRequest(String endpointUrl, Map<String, String> queryParams) {
    ClientConfiguration configuration = new ClientConfiguration();
    configuration.setTargetHost(atlasConfiguration.getHost());
    configuration.setUnsafeTargetPort(atlasConfiguration.getPort().toString());
    configuration.setUnsafe(!atlasConfiguration.isSecure());

    HttpAuthenticationFeature credentials =
        HttpAuthenticationFeature.basicBuilder()
            .credentials(atlasConfiguration.getUsername(), atlasConfiguration.getPassword())
            .build();

    HttpClientFactory clientFactory = new HttpClientFactory(configuration, credentials);

    String fetchUrl = API_URL + endpointUrl;
    WebTarget target = clientFactory.getTarget(fetchUrl);
    if (queryParams != null) {
      for (String key : queryParams.keySet()) {
        target = target.queryParam(key, queryParams.get(key));
      }
    }

    return target.request().get();
  }


  public Map getTypeDefs() {
    String enpointURI = "types/typedefs";
    Response result = executeGetRequest(enpointURI, null);

    if (result.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new LcmException("Unable to get Type Definitions from Atlas. Resonce code:"
          + result.getStatus());
    }

    return result.readEntity(Map.class);
  }


  public Map searchEntitiesByType(String type) {
    String enpointURI = "search/basic";
    Map<String, String> params = new HashMap();
    params.put("typeName", "hive_table");

    Response result = executeGetRequest(enpointURI, params);

    if (result.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new LcmException("Unable to get Type Definitions from Atlas. Resonce code:"
          + result.getStatus());
    }

    return result.readEntity(Map.class);
  }

  public Map getEntity(String uId) {
    String endpointURI = "entity/guid/" + uId;
    Response result = executeGetRequest(endpointURI, null);
    if (result.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new LcmException("Unable to get Type Definitions from Atlas. Resonce code:"
          + result.getStatus());

    }
    return result.readEntity(Map.class);
  }
}
