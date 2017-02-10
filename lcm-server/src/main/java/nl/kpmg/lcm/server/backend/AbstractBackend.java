/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.backend;

import nl.kpmg.lcm.server.data.meatadata.MetaData;
import nl.kpmg.lcm.server.data.meatadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author mhoekstra
 */
abstract class AbstractBackend implements Backend {
  protected final MetaDataWrapper metaDataWrapper;
  protected final URI dataURI;


  protected AbstractBackend(MetaData metaData) {
    metaDataWrapper = new MetaDataWrapper(metaData);
    Notification validationNotification = new Notification();
    validation(metaDataWrapper, validationNotification);
    if (validationNotification.hasErrors()) {
      throw new LcmValidationException(validationNotification);
    }
    this.dataURI = parseDataUri(metaDataWrapper.getData().getUri());

  }

  private void validation(MetaDataWrapper metaDataWrapper, Notification notification) {
    validateMetadata(metaDataWrapper, notification);
    extraValidation(metaDataWrapper, notification);
  }

  /***
   * Override this method to ensure that the passed metaDataWrapper is compatible with your implementation
   * of the backend
   *
   * @param metaDataWrapper
   */
  private void validateMetadata(MetaDataWrapper metaDataWrapper, Notification notification) {
    if (metaDataWrapper.isEmpty()) {
      notification.addError("The metaData could not be null!", null);
      return;
    }

    if (metaDataWrapper.getData().getUri() == null || metaDataWrapper.getData().getUri().isEmpty()) {
      notification.addError("The metaDataWrapper data uri could not be null!", null);
      return;
    }

    try {
      URI parsedUri = new URI(metaDataWrapper.getData().getUri());
      if (!getSupportedUriSchema().equals(parsedUri.getScheme())) {
        notification.addError(String.format(
            "Detected uri schema (%s) doesn't match with this backends supported uri schema (%s)",
            parsedUri.getScheme(), getSupportedUriSchema()), null);
      }
    } catch (URISyntaxException ex) {
      notification.addError(String.format("Unable to parse URI (%s) ", metaDataWrapper.getData().getUri()), ex);
    }

  }

  /***
   * Override this method to ensure that the passed metaDataWrapper and storage are compatible with your
   * implementation of the backend
   *
   * @param storage
   * @param metaDataWrapper
   */
  protected abstract void extraValidation(MetaDataWrapper metaDataWrapper, Notification notification);

  protected abstract String getSupportedUriSchema();

  protected URI getDataUri() {
    return dataURI;
  }

  private URI parseDataUri(String uri) throws LcmException {

    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      throw new LcmException(String.format("Failure while trying to parse URI '%s'", uri), ex);
    }
  }

}
