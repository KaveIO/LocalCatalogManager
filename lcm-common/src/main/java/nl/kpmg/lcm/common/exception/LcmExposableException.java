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
package nl.kpmg.lcm.common.exception;

import javax.ws.rs.core.Response;

/**
 * This exception is used for unrecoverable cases i.e. in cases(except validation) when the flow
 must be interrupted and an error message must be returned to the REST API user. For example: the
 data storage is not found or the requested data source is not supported etc. Be careful when
 constructing LcmExposableException, the error message is passed directly to the REST API end user!
 *
 * @author shristov
 */
public class LcmExposableException extends LcmException {

  private Response.Status responseCode;

  /**
   * @param message is passed directly to the REST API end user!It must be meaningful message for
   *        the end user. Additionally the message must not expose any implementation details.
   */
  public LcmExposableException(String message) {
    super(message);
  }

  /**
   * @param message is passed directly to the REST API end user!It must be meaningful message for
   *        the end user. Additionally the message must not expose any implementation details.
   * @param responseCode is valid  Response.Status that will be return to the end user.
   */
  public LcmExposableException(String message, Response.Status responseCode) {
    super(message);
    this.responseCode = responseCode;
  }

    /**
   * @param message is passed directly to the REST API end user!It must be meaningful message for
   *        the end user. Additionally the message must not expose any implementation details.
   * @param errorCode is valid Response.Status that will be return to the end user.
   */
  public LcmExposableException(String message, Response.Status errorCode, Exception cause) {
    super(message, cause);
    this.responseCode = errorCode;
  }

    /**
     * @return the responseCode
     */
    public Response.Status getResponseCode() {
        return responseCode;
    }
}
