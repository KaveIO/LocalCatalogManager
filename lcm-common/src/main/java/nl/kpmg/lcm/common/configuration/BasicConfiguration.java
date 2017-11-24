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

package nl.kpmg.lcm.common.configuration;

public abstract class BasicConfiguration {

  protected String serviceName;
  protected Integer servicePort;
  protected Integer secureServicePort;

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

  public boolean isUnsafe() {
    return unsafe;
  }

  public void setUnsafe(boolean unsafe) {
    this.unsafe = unsafe;
  }

}
