/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server;

import nl.kpmg.lcm.BasicConfiguration;

/**
 *
 * @author mhoekstra
 */
public class Configuration extends BasicConfiguration {

    private String serverStorage;

    private Boolean withClientAuth = false;


    public String getServerStorage() {
        return serverStorage;
    }

    public void setServerStorage(String serverStorage) {
        this.serverStorage = serverStorage;
    }


    public void setClientAuth(Boolean clientAuth) {
        withClientAuth = clientAuth;
    }

    public Boolean getWithClientAuth() {
        return withClientAuth;
    }    

}
