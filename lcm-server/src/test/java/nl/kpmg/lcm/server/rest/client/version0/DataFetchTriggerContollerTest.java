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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.configuration.ServerConfiguration;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.common.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.data.service.TaskDescriptionService;
import nl.kpmg.lcm.server.test.mock.MetaDataMocker;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public class DataFetchTriggerContollerTest extends LcmBaseServerTest {

  @Autowired
  private TaskDescriptionService taskDescriptionService;

  private static final String AUTH_USER_HEADER = "LCM-Authentication-User";
  private static final String METADATA_CONTENT_TYPE =
      "application/nl.kpmg.lcm.server.data.MetaData+json";
  private static final String METADATA_PATH = "client/v0/local";
  private static final String TRIGGER_PATH = "client/v0/remote";
  private static final Logger LOGGER = LoggerFactory.getLogger(DataFetchTriggerContollerTest.class
      .getName());
  private static final String CSV_SCHEME = DataFormat.CSV;
  private static final String CSV_STORAGE_PATH = System.getProperty("java.io.tmpdir");
  private static final String CSV_STORAGE_NAME = "csv-storage";
  private static final String CSV_FILE_NAME = "temp.csv";
  private static final String CSV_STORAGE_URI = CSV_SCHEME + "://" + CSV_STORAGE_NAME + "/"
      + CSV_FILE_NAME;
  private static final File CSV_FILE = new File(CSV_STORAGE_PATH + File.separator + CSV_FILE_NAME);
  private static final String LCM_PATH = "client/v0/remoteLcm";
  private static final String LCM_CONTENT_TYPE =
      "application/nl.kpmg.lcm.server.data.RemoteLcm+json";

  private int dataLen;
  private int headerLen;

  @Autowired
  private StorageService storageService;

  @Autowired
  private ServerConfiguration serverConfiguration;

  private static MetaDataWrapper md;
  private static Storage csvStorage;
  private static RemoteLcm lcm;

  /**
   * Always clean up DB.
   *
   * @throws ServerException
   */
  @After
  public void afterTest() throws ServerException {
    getWebTarget().path("client/logout").request().header(AUTH_USER_HEADER, "admin")
        .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(null);
    taskDescriptionService.deleteAll();
  }

  @Before
  public void beforeTest() throws ServerException, IOException {
    if (csvStorage == null) {
      // Client finds the id of the remote lcm that contains the data she wants
      lcm = getLCMId();
      csvStorage = StorageMocker.createCsvStorage();
      md = createStorageAndPostMetadata(csvStorage);
    }
  }

  @Test
  public void testTrigger() throws ServerException, IOException {
    // Client finds the id of the remote lcm that contains the data she wants
    RemoteLcm lcm = getLCMId();
    Storage csvStorage = addStorageIfDoesNotExists(StorageMocker.createCsvStorage());
    MetaDataWrapper md = postMetadata();

    // Sends a request to local lcm to fetch the data and metadata
    postTrigger(lcm.getId(), md.getId(), csvStorage.getId(), 200);

    // In the end we should have a task scheduled
    List<TaskDescription> tdList = taskDescriptionService.findAll();
    assertNotNull(tdList);
    assertTrue(tdList.size() > 0);
    assertNotNull(tdList.get(0));
  }

  @Test
  public void testNonExistingLcm() throws ServerException, IOException {
    // Client disovers after metadata id from the remote lcm

    Storage csvStorage = addStorageIfDoesNotExists(StorageMocker.createCsvStorage());
    MetaDataWrapper md = postMetadata();
    // Sends a request to local lcm to fetch the data and metadata
    postTrigger("non-existing-lcm", md.getId(), csvStorage.getId(), 404);
  }

  @Test
  public void testNonExistingMetadata() throws ServerException, IOException {
    RemoteLcm lcm = getLCMId();
    Storage csvStorage = addStorageIfDoesNotExists(StorageMocker.createCsvStorage());
    // Sends a request to local lcm to fetch the data and metadata
    postTrigger(lcm.getId(), "non-existing-metadata", csvStorage.getId(), 404);
  }

  private Storage addStorageIfDoesNotExists(Storage csvStorage) {
    Storage savedCsvStorage = storageService.findByName(csvStorage.getName());
    if (savedCsvStorage == null) {
      savedCsvStorage = storageService.add(csvStorage);
    }

    return savedCsvStorage;
  }


  @Test
  public void testNonExistingStorage() throws ServerException, IOException {
    RemoteLcm lcm = getLCMId();
    MetaDataWrapper md = postMetadata();
    // Sends a request to local lcm to fetch the data and metadata
    postTrigger(lcm.getId(), md.getId(), "non-existing-storage", 404);
  }

  private MetaDataWrapper postMetadata() throws IOException, ServerException {

    MetaDataWrapper metadataWrapper = MetaDataMocker.getCsvMetaDataWrapper();
    List<String> uriList = new ArrayList();
    uriList.add(CSV_STORAGE_URI);
    String key = MetaDataMocker.getTestKey();
    metadataWrapper.getDynamicData().getDynamicDataDescriptor(key).setURI(uriList.get(0));
    metadataWrapper.getData().setUri(uriList);

    Backend backend = storageService.getBackend(metadataWrapper);

    generateCsvTestFile(CSV_FILE);


    IterativeData data = (IterativeData) backend.read(key);
    assertNotNull(data);
    postMeadata(metadataWrapper, 200);
    metadataWrapper = new MetaDataWrapper(getMetadata(200).get(0).getItem());
    return metadataWrapper;
  }

   private MetaDataWrapper createStorageAndPostMetadata(Storage csvStorage) throws IOException,
      ServerException {

    MetaDataWrapper metadataWrapper = MetaDataMocker.getCsvMetaDataWrapper();
      List<String> uriList = new ArrayList();
        uriList.add(CSV_STORAGE_URI);
        String key = MetaDataMocker.getTestKey();
        metadataWrapper.getDynamicData().getDynamicDataDescriptor(key).setURI(uriList.get(0));
    metadataWrapper.getData().setUri(uriList);

    storageService.add(csvStorage);
    Backend backend = storageService.getBackend(metadataWrapper);

    generateCsvTestFile(CSV_FILE);
    
    IterativeData data = (IterativeData)backend.read(key);
    assertNotNull(data);
    postMeadata(metadataWrapper, 200);
    metadataWrapper = new MetaDataWrapper(getMetadata(200).get(0).getItem());
    return metadataWrapper;
  }

  private void postMeadata(MetaDataWrapper metadataWrapper, int expected) throws ServerException {
    Entity<MetaData> entity = Entity.entity(metadataWrapper.getMetaData(), METADATA_CONTENT_TYPE);

    Response resp =
        getWebTarget().path(METADATA_PATH).request().header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
            .post(entity);

    assertEquals(expected, resp.getStatus());
  }

  private void postTrigger(String lcmId, String metadataId, String storageId, int expected)
      throws ServerException {
    Map payload = new LinkedHashMap();
    payload.put("local-storage-id", storageId);
    payload.put("transfer-settings", "{\"forceOverwrite\": \"true\"}");
    Entity<Map> entity = Entity.entity(payload, "application/json");
    Response resp =
        getWebTarget().path(TRIGGER_PATH).path(lcmId).path("metadata").path(metadataId).request()
            .header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
            .post(entity);
    assertEquals(expected, resp.getStatus());
  }

  private RemoteLcm getLCMId() throws ServerException {
    RemoteLcm lcm = new RemoteLcm();
    lcm.setId("uid" + 0);
    lcm.setDomain(serverConfiguration.getServiceName());
    if (serverConfiguration.isUnsafe()) {
      lcm.setPort(serverConfiguration.getServicePort());
      lcm.setProtocol("http");
    } else {
      lcm.setPort(serverConfiguration.getSecureServicePort());
      lcm.setProtocol("https");
    }
    postLcm(lcm, 200);

    return lcm;
  }

  private void postLcm(RemoteLcm lcm, int expected) throws ServerException {
    Entity<RemoteLcm> entity = Entity.entity(lcm, LCM_CONTENT_TYPE);
    Response resp =
        getWebTarget().path(LCM_PATH).request().header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
            .post(entity);
    assertEquals(expected, resp.getStatus());
  }

  private List<MetaDataRepresentation> getMetadata(int expected) throws ServerException {

    Invocation.Builder req =
        getWebTarget().path(METADATA_PATH).request().header(AUTH_USER_HEADER, "admin")
            .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin);

    Response response = req.get();
    assertEquals(expected, response.getStatus());
    List<MetaDataRepresentation> items =
        response.readEntity(MetaDatasRepresentation.class).getItems();

    return items;
  }

  private void generateCsvTestFile(File f) throws IOException {
    headerLen = 5;
    dataLen = 10;
    try (PrintWriter out = new PrintWriter(f)) {
      for (int i = 0; i < headerLen; i++) {
        if (i < headerLen - 1) {
          out.print("Header" + i + ",");
        } else {
          out.print("Header" + i + "\n");
        }
      }

      for (int i = 0; i < dataLen; i++) {
        for (int j = 0; j < headerLen; j++) {
          if (j < headerLen - 1) {
            out.print(i + "" + j + ",");
          } else {
            out.print(i + "" + j + "\n");
          }
        }
      }
    }
  }

}
