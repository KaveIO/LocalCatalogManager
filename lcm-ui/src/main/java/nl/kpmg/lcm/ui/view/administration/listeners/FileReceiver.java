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
package nl.kpmg.lcm.ui.view.administration.listeners;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.ClientException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 *
 * @author shristov
 */


public class FileReceiver implements Upload.Receiver, Upload.SucceededListener,
    Upload.FailedListener {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileReceiver.class
      .getName());
  public ByteArrayOutputStream certificate = new ByteArrayOutputStream();

  private Window contianer;
  private RestClientService restClientService;
  private String alias;
  private boolean isUpdate;

  public FileReceiver(Window contianer, final RestClientService restClientService, boolean isUpdate) {
    this.contianer = contianer;
    this.restClientService = restClientService;
    this.isUpdate = isUpdate;
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType) {
    // Create and return a file output stream
    return certificate;
  }

  @Override
  public void uploadSucceeded(Upload.SucceededEvent event) {
    try {

      if (isUpdate) {
        restClientService.updateCertificateAlias(alias,
            new ByteArrayInputStream(certificate.toByteArray()));
      } else {
        restClientService.addCertificateAlias(alias,
            new ByteArrayInputStream(certificate.toByteArray()));
      }
    } catch (ServerException | DataCreationException | AuthenticationException | ClientException ex) {
      Notification.show("Operation of failed!");
      LOGGER.warn("Submitting certificate failed.", ex);
    }
    Notification.show("Operation finished successfully.");
    contianer.close();
  }

  @Override
  public void uploadFailed(Upload.FailedEvent event) {
    Notification.show("Operation of failed!");
    LOGGER.warn("Certificate upload of remote LCM failed." + event.getReason().getMessage());

  }

  /**
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @param alias the alias to set
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }
}