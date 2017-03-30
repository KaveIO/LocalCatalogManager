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

import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mhoekstra
 */
abstract class AbstractBackend implements Backend {
  protected final MetaDataWrapper metaDataWrapper;
  protected ProgressIndicationFactory progressIndicationFactory;

  protected AbstractBackend(MetaData metaData) {
    metaDataWrapper = new MetaDataWrapper(metaData);
    Notification validationNotification = new Notification();
    validation(metaDataWrapper, validationNotification);
    if (validationNotification.hasErrors()) {
      throw new LcmValidationException(validationNotification);
    }

  }

  private void validation(MetaDataWrapper metaDataWrapper, Notification notification) {
     if (metaDataWrapper.isEmpty()) {
      notification.addError("The metaData could not be null!", null);
      return;
    }

    try {
      URI parsedUri = new URI(metaDataWrapper.getData().getUri());
      if (!getSupportedUriSchema().contains(parsedUri.getScheme())) {
        notification.addError(String.format(
            "Detected uri schema (%s) doesn't match with this backends supported uri schema (%s)",
            parsedUri.getScheme(), getSupportedUriSchema()), null);
      }
    } catch (URISyntaxException ex) {
      notification.addError(String.format("Unable to parse URI (%s) ", metaDataWrapper.getData().getUri()), ex);
    }
  }

  public Set<String> getSupportedUriSchema() {
      Set<String> supportedSchemas =  new  HashSet();
      BackendSource sourceAnnotation = this.getClass().getAnnotation(BackendSource.class);
      if(sourceAnnotation != null) {
        for(String type: sourceAnnotation.type()) {
            supportedSchemas.add(type);
        }
      }

    return supportedSchemas;
  }

  @Override
  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory){
      this.progressIndicationFactory = progressIndicationFactory;
  }

}
