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

package nl.kpmg.lcm.server.rest.client.version0;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Response;

/**
 * A helper class to map http codes to exceptions
 *
 * @author S. Koulouzis
 */
public class HttpResponseHandler {

  /**
   * Given a Response will throw the appropriate exception
   *
   * @param response
   * @throws ClientErrorException
   */
  public static void handleResponse(Response response) throws ClientErrorException {
    int status = response.getStatus();
    //Don't bother looking 
    if (status < 400) {
      return;
    }
    switch (status) {
      case 400:
        throw new BadRequestException(response);
      case 401:
        throw new NotAuthorizedException(response);
      case 403:
        throw new ForbiddenException(response);
      case 404:
        throw new NotFoundException(response);
      case 405:
        throw new NotAllowedException(response);
      case 406:
        throw new NotAcceptableException(response);
      case 500:
        throw new InternalServerErrorException(response);
      case 501:
        throw new ServiceUnavailableException(response);
      default:
        throw new ClientErrorException(response);
    }
  }

}
