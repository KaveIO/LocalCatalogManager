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

package nl.kpmg.lcm.configuration;

public abstract class BasicConfiguration {

  protected String serviceName;
  protected Integer servicePort;
  protected Integer secureServicePort;

  protected String keystore;
  protected String keystoreType;
  protected String keystorePassword;
  protected String keystoreAlias;
  protected String keystoreKeypass;
  protected String truststore;
  protected String truststoreType;
  protected String truststorePassword;

  protected boolean unsafe;

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String ServiceName) {
    this.serviceName = ServiceName;
  }

  public Integer getServicePort() {
    return servicePort;
  }

  public void setServicePort(Integer ServicePort) {
    this.servicePort = ServicePort;
  }

  public Integer getSecureServicePort() {
    return secureServicePort;
  }

  public void setSecureServicePort(Integer secureServicePort) {
    this.secureServicePort = secureServicePort;
  }

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

  public String getTruststore() {
    return truststore;
  }

  public void setTruststore(String truststore) {
    this.truststore = truststore;
  }

  public String getTruststoreType() {
    return truststoreType;
  }

  public void setTruststoreType(String truststoreType) {
    this.truststoreType = truststoreType;
  }

  public String getTruststorePassword() {
    return truststorePassword;
  }

  public void setTruststorePassword(String truststorePassword) {
    this.truststorePassword = truststorePassword;
  }

  public boolean isUnsafe() {
    return unsafe;
  }

  public void setUnsafe(boolean unsafe) {
    this.unsafe = unsafe;
  }

}
