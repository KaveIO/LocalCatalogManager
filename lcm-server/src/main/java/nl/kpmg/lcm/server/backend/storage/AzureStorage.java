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
package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.validation.Notification;

/**
 *
 * @author shristov
 */
public class AzureStorage extends AbstractStorageContainer{

    private String accountFQDN;
    private String clientId;
    private String authTokenEndpoint;
    private String clientKey;

    public AzureStorage(Storage storage){
        super(storage);
    }

    public String getAccountFQDN() {
        return accountFQDN;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAuthTokenEndpoint() {
        return authTokenEndpoint;
    }

    public String getClientKey() {
        return clientKey;
    }


    @Override
    protected void validate(Storage storage, Notification notification) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
