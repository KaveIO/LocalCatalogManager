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

package nl.kpmg.lcm.common.configuration;

/**
 *
 * @author mhoekstra
 */
public class ServerConfiguration extends BasicConfiguration {

  private String serverStorage;
  private String applicationName;

  private String keystore;
  private String keystoreType;
  private String keystorePassword;
  private String keystoreAlias;
  private String keystoreKeypass;

  public String getKeystore() {
    return keystore;
  }

  public void setKeystore(String keystore) {
    this.keystore = keystore;
  }

  public String getKeystoreType() {
    return keystoreType;
  }

  public void setKeystoreType(String keystoreType) {
    this.keystoreType = keystoreType;
  }

  public String getKeystorePassword() {
    return keystorePassword;
  }

  public void setKeystorePassword(String keystorePassword) {
    this.keystorePassword = keystorePassword;
  }

  public String getKeystoreAlias() {
    return keystoreAlias;
  }

  public void setKeystoreAlias(String keystoreAlias) {
    this.keystoreAlias = keystoreAlias;
  }

  public String getKeystoreKeypass() {
    return keystoreKeypass;
  }

  public void setKeystoreKeypass(String keystoreKeypass) {
    this.keystoreKeypass = keystoreKeypass;
  }

  public String getServerStorage() {
    return serverStorage;
  }

  public void setServerStorage(String serverStorage) {
    this.serverStorage = serverStorage;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

}
