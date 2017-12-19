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
package nl.kpmg.lcm.server.data.service;

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_REMOTE_USER_HEADER;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.HttpsClientFactory;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.server.rest.authorization.PermissionChecker;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author shristov
 */
@Service
public class RemoteDataDeletionService {
  @Autowired
  private RemoteLcmService lcmService;

  @Autowired
  private LcmIdService lcmIdService;

  @Autowired
  private ClientConfiguration configuration;

  private HttpAuthenticationFeature credentials;

  private final String REMOTE_DATA_PATH = "remote/v0/data";

  public boolean deleteRemoteData(String remoteLcmId, String metadataId) {

    try {
      RemoteLcm lcm = lcmService.findOneById(remoteLcmId);
      WebTarget webTarget = getWebTarget(lcm).path(REMOTE_DATA_PATH).path(metadataId);

      String username = PermissionChecker.getThreadLocal().get().getName();
      String self = lcmIdService.getLcmIdObject().getLcmId();
      Response response =
          webTarget.request().header(LCM_AUTHENTICATION_REMOTE_USER_HEADER, username)
              .header(LCM_AUTHENTICATION_ORIGIN_HEADER, self).delete();

      if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
        return true;
      }
    } catch (ServerException ex) {
      throw new LcmException("Unable to delete remote data", ex);
    }

    return false;
  }

  private WebTarget getWebTarget(RemoteLcm lcm) throws ServerException {
    if (credentials == null) {
      credentials =
          HttpAuthenticationFeature.basicBuilder()
              .credentials(lcm.getApplicationId(), lcm.getApplicationKey()).build();
    }
    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);
    configuration.setTargetHost(lcm.getDomain());
    configuration.setTargetPort(lcm.getPort().toString());
    return clientFactory.createWebTarget(buildRemoteUrl(lcm));
  }

  private String buildRemoteUrl(RemoteLcm lcm) {
    String url = String.format("%s://%s", lcm.getProtocol(), lcm.getDomain());
    if (lcm.getPort() != null) {
      url += ":" + lcm.getPort();
    }
    return url;
  }

}