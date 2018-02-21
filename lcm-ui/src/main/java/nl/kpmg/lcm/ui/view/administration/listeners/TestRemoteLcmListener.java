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

import com.vaadin.ui.Button;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.common.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

import org.slf4j.LoggerFactory;

/**
 *
 * @author shristov
 */
public class TestRemoteLcmListener extends AbstractListener {
  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(TestRemoteLcmListener.class.getName());

  /**
   * @param dataContainer parent view.
   */
  public TestRemoteLcmListener(final DynamicDataContainer dataContainer,
      final RestClientService restClientService) {
    super(dataContainer, restClientService);
  }

  @Override
  public void buttonClick(final Button.ClickEvent event) {
    RemoteLcmRepresentation data = (RemoteLcmRepresentation) event.getButton().getData();
    RemoteLcm item = data.getItem();
    TestResult result = restClientService.testRemoteLcm(item.getId());
    dataContainer.updateContent();
    String message = String.format("%s : %s", result.getCode(), result.getMessage());
    com.vaadin.ui.Notification.show(message);

  }

  protected String getItemName() {
    return "remote LCM";
  }

  protected void deleteItem(String id) throws AuthenticationException, ServerException,
      LcmBadRequestException {
    restClientService.deleteRemoteLcm(id);
  }
}
