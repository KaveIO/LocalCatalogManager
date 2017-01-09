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

package nl.kpmg.lcm.configuration;

/**
 *
 * @author mhoekstra
 */
public class ClientConfiguration extends BasicConfiguration {

  private String targetHost;
  private String targetPort;
  private String unsafeTargetPort;

  public String getTargetHost() {
    return targetHost;
  }

  public void setTargetHost(String targetHost) {
    this.targetHost = targetHost;
  }

  public String getTargetPort() {
    return targetPort;
  }

  public void setTargetPort(String targetPort) {
    this.targetPort = targetPort;
  }

  public String getUnsafeTargetPort() {
    return unsafeTargetPort;
  }

  public void setUnsafeTargetPort(String unsafePort) {
    this.unsafeTargetPort = unsafePort;
  }

}
