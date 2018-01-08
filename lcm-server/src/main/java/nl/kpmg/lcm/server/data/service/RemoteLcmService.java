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

package nl.kpmg.lcm.server.data.service;

import jersey.repackaged.com.google.common.collect.Lists;

import nl.kpmg.lcm.common.client.HttpsClientFactory;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.server.data.dao.RemoteLcmDao;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.ws.rs.core.Response;

/**
 *
 * @author S. Koulouzis
 */
@Service
public class RemoteLcmService {
  private static final Logger logger = LoggerFactory.getLogger(RemoteLcmService.class.getName());

  @Autowired
  private RemoteLcmDao dao;

  @Autowired
  private ClientConfiguration configuration;

  // TODO once the Authorization/Auhtenticaion model is implemented this part must be refactored
  // After the refactoring ther emust be used a user which is used only for remote calls
  private String adminUser;
  private String adminPassword;


  @Value("${lcm.server.adminUser}")
  public final void setAdminUser(final String adminUser) {
    this.adminUser = adminUser;
  }

  @Value("${lcm.server.adminPassword}")
  public final void setAdminPassword(final String adminPassword) {
    this.adminPassword = adminPassword;
  }

  public List<RemoteLcm> findAll() {
    return Lists.newLinkedList(dao.findAll());
  }

  public RemoteLcm findOneById(String id) {
    return dao.findOne(id);
  }

  public RemoteLcmDao getDao() {
    return dao;
  }

  public TestResult testRemoteLcmConnectivity(String id) {
    RemoteLcm remoteLcm = dao.findOne(id);
    configuration.setTargetHost(remoteLcm.getDomain());
    configuration.setTargetPort(remoteLcm.getPort().toString());
    String fetchUrl = buildRemoteUrl(remoteLcm) + "/remote/v0/test";

    HttpAuthenticationFeature credentials =
        HttpAuthenticationFeature.basicBuilder().credentials(adminUser, adminPassword).build();

    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);

    Response response = null;
    TestResult result;
    try {
      response = clientFactory.createWebTarget(fetchUrl).request().get();
      if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
        String message = response.readEntity(String.class);
        result = new TestResult(message, TestResult.TestCode.ACCESIBLE);
      } else {
        String message = "Unable to reach it: " + response.getStatusInfo().getFamily().name();
        result = new TestResult(message, TestResult.TestCode.INACCESSIBLE);
      }
    } catch (Exception ex) {
      logger.error("Test connection failed. Error " + ex.getMessage());
      result = new TestResult(ex.getMessage(), TestResult.TestCode.INACCESSIBLE);
    }

    remoteLcm.setStatus(String.format("%s : %s", result.getCode(), result.getMessage()));
    dao.save(remoteLcm);
    return result;
  }

  private String buildRemoteUrl(RemoteLcm lcm) {
    String url = String.format("%s://%s", lcm.getProtocol(), lcm.getDomain());
    if (lcm.getPort() != null) {
      url += ":" + lcm.getPort();
    }
    return url;
  }
}