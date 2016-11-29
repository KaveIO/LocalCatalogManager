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

import nl.kpmg.lcm.validation.Notification;
import java.net.URI;
import java.net.URISyntaxException;
import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.data.MetaData;

/**
 *
 * @author mhoekstra
 */
abstract class AbstractBackend implements Backend {
  protected final MetaData metaData;
  protected final URI dataURI;


  protected AbstractBackend(MetaData metaData) throws BackendException {
    Notification validationNotification = new Notification();
    validation(metaData, validationNotification);
    if (validationNotification.hasErrors()) {
      throw new BackendException(validationNotification.errorMessage());
    }

    this.metaData = metaData;
    this.dataURI = parseDataUri(metaData.getDataUri());

  }

  private void validation(MetaData metaData, Notification notification) {
    validateMetadata(metaData, notification);
    extraValidation(metaData, notification);
  }

  /***
   * Override this method to ensure that the passed metaData is compatible with your implementation
   * of the backend
   * 
   * @param metaData
   * @throws BadMetaDataException
   */
  private void validateMetadata(MetaData metaData, Notification notification) {
    if (metaData == null) {
      notification.addError("The metaData could not be null!", null);
      return;
    }

    if (metaData.getDataUri() == null) {
      notification.addError("The metaData data uri could not be null!", null);
      return;
    }

    try {
      URI parsedUri = new URI(metaData.getDataUri());
      if (!getSupportedUriSchema().equals(parsedUri.getScheme())) {
        notification.addError(String.format(
            "Detected uri schema (%s) doesn't match with this backends supported uri schema (%s)",
            parsedUri.getScheme(), getSupportedUriSchema()), null);
      }
    } catch (URISyntaxException ex) {
      notification.addError(String.format("Unable to parse URI (%s) ", metaData.getDataUri()), ex);
    }

  }

  /***
   * Override this method to ensure that the passed metaData and storage are compatible with your
   * implementation of the backend
   * 
   * @param storage
   * @param metaData
   * @throws BadMetaDataException
   */
  protected abstract void extraValidation(MetaData metaData,
      Notification notification);

  protected abstract String getSupportedUriSchema();

  protected URI getDataUri() {
    return dataURI;
  }

  private URI parseDataUri(String uri) throws BackendException {

    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      throw new BackendException(String.format("Failure while trying to parse URI '%s'", uri), ex);
    }
  }

}
