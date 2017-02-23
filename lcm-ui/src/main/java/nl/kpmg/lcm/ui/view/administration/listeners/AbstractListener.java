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

import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

/**
 *
 * @author shristov
 */
public abstract class AbstractListener implements Button.ClickListener {
    protected final DynamicDataContainer dataContainer;
    protected final RestClientService restClientService;

    /**
     * @param dataContainer parent view.
     */
    public AbstractListener(final DynamicDataContainer dataContainer,
            final RestClientService restClientService) {
      this.dataContainer = dataContainer;
      this.restClientService = restClientService;
    }
}
