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
import static org.junit.Assert.assertNotNull;

import com.google.gson.stream.JsonReader;

import nl.kpmg.lcm.rest.types.FetchEndpointRepresentation;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.DataTransformationSettings;
import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.backend.exception.BackendNotImplementedException;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.FetchEndpoint;
import nl.kpmg.lcm.server.data.JsonReaderContentIterator;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.service.FetchEndpointService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.data.service.exception.MissingStorageException;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public class FetchEndpointContollerTest extends LcmBaseServerTest {

  @Autowired
  private FetchEndpointService fetchEndpointService;

  @Autowired
  private StorageService storageService;

  private static final String FETCH_PATH = "remote/v0/fetch";
  private static final String GENERATE_FETCH_PATH = "/remote/v0/metadata";
  private static final String METADATA_PATH = "client/v0/local";
  private static final String AUTH_USER_HEADER = "LCM-Authentication-User";
  private static final String METADATA_CONTENT_TYPE =
      "application/nl.kpmg.lcm.server.data.MetaData+json";

  private static final Logger LOG = Logger.getLogger(FetchEndpointContollerTest.class.getName());

  private static final String CSV_SCHEME = "csv";
  private static final String CSV_STORAGE_PATH = System.getProperty("java.io.tmpdir");
  private static final String TMP = System.getProperty("java.io.tmpdir");
  private static final String CSV_STORAGE_NAME = "csv-storage";
  private static final String CSV_FILE_NAME = "temp.csv";
  private static final String CSV_STORAGE_URI =
      CSV_SCHEME + "://" + CSV_STORAGE_NAME + "/" + CSV_FILE_NAME;
  private static final File CSV_FILE = new File(CSV_STORAGE_PATH + File.separator + CSV_FILE_NAME);
  private int dataLen;
  private int headerLen;

  /**
   * Always clean up DB.
   *
   * @throws ServerException
   */
  @After
  public void afterTest() throws ServerException {
    getWebTarget().path("client/logout").request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(null);

    List<FetchEndpoint> all = fetchEndpointService.findAll();
    for (FetchEndpoint fe : all) {
      if (fe != null) {
        fetchEndpointService.getDao().delete(fe);
      }
    }

    List<Storage> allStorage = storageService.findAll();
    for (Storage s : allStorage) {
      if (s != null) {
        storageService.getStorageDao().delete(s);
      }
    }
  }

  @AfterClass
  public static void afterClass() {
    if (CSV_FILE != null) {
      CSV_FILE.delete();
    }
  }

  @Test
  public void testExpiration() throws ServerException, MissingStorageException, BackendException,
      BackendNotImplementedException, IOException, InterruptedException {
    FetchEndpoint fe = addTestFetchEndpoint();
    Thread.sleep(1);
    getFetchURL(fe.getId(), 404);
  }

  @Test
  public void testGetFetchURL() throws MissingStorageException, BackendException,
      BackendNotImplementedException, IOException, ServerException {
    createStorgaeAndPostMetadata();
    // Discover metadata
    List<MetaDataRepresentation> list = getMetadata(200);
    MetaData md = list.get(0).getItem();
    // Get metadata id
    String id = md.getId();
    // Get transfer token
    FetchEndpoint token = generateFetchURL(id, 200);
    // Download the file
    Response resp = getFetchURL(token.getId(), 200);

    Backend backend;
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(resp.readEntity(InputStream.class), "UTF-8"))) {
      JsonReader reader = new JsonReader(br);
      ContentIterator iter = new JsonReaderContentIterator(reader);

      backend = storageService.getBackend(md);
      backend.store(iter, new DataTransformationSettings(), true);
    }

    Data data = backend.read();
    ContentIterator di = data.getIterator();
    int numOfLines = 0;

    while (di.hasNext()) {
      Map map = di.next();
      Set keys = map.keySet();
      int numOfKeys = 0;
      for (Object o : keys) {
        numOfKeys++;
      }
      assertEquals(headerLen, numOfKeys);
      numOfLines++;
    }
    assertEquals(dataLen, numOfLines);

  }

  private List<MetaDataRepresentation> getMetadata(int expected) throws ServerException {

    Invocation.Builder req =
        getWebTarget().path(METADATA_PATH).request().header(AUTH_USER_HEADER, "admin")
            .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin);

    Response response = req.get();
    assertEquals(expected, response.getStatus());
    List<MetaDataRepresentation> items =
        response.readEntity(MetaDatasRepresentation.class).getItems();

    return items;
  }

  private MetaData createStorgaeAndPostMetadata() throws BackendNotImplementedException,
      MissingStorageException, BackendException, IOException, ServerException {
    Storage csvStorage = new Storage();
    csvStorage.setName(CSV_STORAGE_NAME);
    Map options = new HashMap();
    options.put("storagePath", CSV_STORAGE_PATH);
    csvStorage.setOptions(options);

    MetaData metadata = new MetaData();
    metadata.setDataUri(CSV_STORAGE_URI);

    storageService.getStorageDao().save(csvStorage);
    Backend backend = storageService.getBackend(metadata);

    generateCsvTestFile(CSV_FILE);
    Data data = backend.read();
    assertNotNull(data);
    postMeadata(metadata, 200);
    metadata = getMetadata(200).get(0).getItem();
    return metadata;
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

  private Response getFetchURL(String id, int expected)
      throws ServerException, FileNotFoundException, IOException {
    Response response = getWebTarget().path(FETCH_PATH).path(id).request()
        .header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(expected, response.getStatus());

    return response;

  }

  private void postMeadata(MetaData metadata, int expected) throws ServerException {
    Entity<MetaData> entity = Entity.entity(metadata, METADATA_CONTENT_TYPE);

    Response resp = getWebTarget().path(METADATA_PATH).request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
        .post(entity);

    assertEquals(expected, resp.getStatus());
  }

  private FetchEndpoint generateFetchURL(String metadataId, int expected) throws ServerException {
    Response response = getWebTarget().path(GENERATE_FETCH_PATH).path(metadataId).path("fetchUrl")
        .request().header(AUTH_USER_HEADER, "admin")
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(expected, response.getStatus());
    FetchEndpointRepresentation fEr = response.readEntity(FetchEndpointRepresentation.class);
    return fEr.getItem();
  }

  private FetchEndpoint addTestFetchEndpoint() throws MissingStorageException, BackendException,
      BackendNotImplementedException, IOException, ServerException {

    MetaData md = createStorgaeAndPostMetadata();

    FetchEndpoint fe = new FetchEndpoint();
    Date now = new Date();
    fe.setCreationDate(new Date());

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(now);
    calendar.add(Calendar.MILLISECOND, 1);
    Date later = calendar.getTime();

    fe.setTimeToLive(later);
    fe.setUserToConsume("user");
    fe.setMetadataId(md.getId());
    fetchEndpointService.getDao().save(fe);
    return fe;
  }

  private File downloadFile(Response resp) throws IOException {
    String header = resp.getHeaderString("Content-Disposition");
    String[] parts = header.split(";");
    String filename = "attachment.json";
    for (String p : parts) {
      if (p.contains("filename=")) {
        filename = p.split("filename=")[1];
        break;
      }
    }
    File jsonFile = null;
    try (InputStream in = resp.readEntity(InputStream.class)) {
      jsonFile = new File(TMP + File.separator + filename);
      try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
        byte buf[] = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          fos.write(buf, 0, len);
        }
      }
    }

    return jsonFile;

  }

}
