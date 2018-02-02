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

package nl.kpmg.lcm.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Catch all exception mapper for the REST interface. By default uncaught exceptions are not
 * displayed by grizzly. This is nice but terrible for debugging. This mapper makes sure all
 * uncaught exceptions are at least logged as a warning. This has a drawback that in some cases
 * default behavior is lost (404 messages).
 *
 * @author mhoekstra
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Throwable> {
  /**
   * The logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GeneralExceptionMapper.class);

  /**
   * Translates an exception to a response.
   *
   * @param ex the caught exception
   * @return the response to provide.
   */
  @Override
  public final Response toResponse(final Throwable ex) {
    Response response;
    if (NotFoundException.class.isAssignableFrom(ex.getClass())) {
      LOGGER.warn("Resource not found on server! Error message: {}", ex.getMessage());
      LOGGER.debug("Stacktrace ", ex);
      response = Response.status(Response.Status.NOT_FOUND).type("text/plain")
          .entity("Resource not found on server.").build();
    } else if (ForbiddenException.class.isAssignableFrom(ex.getClass())) {
      LOGGER.warn("Access is forbidden for the user role! Error message: {}", ex.getMessage());
      LOGGER.debug("Stacktrace ", ex);
      response = Response.status(Response.Status.FORBIDDEN).type("text/plain")
          .entity("Request failed! Check logs for more information.").build();
    } else {
      String logErrorMessage = ex.getMessage() != null ? "Error message: " + ex.getMessage()
          : "" + "Exception type: " + ex.getClass().getName();

      LOGGER.warn("Request failed! Error message: {}", logErrorMessage);
      LOGGER.debug("Stacktrace ", ex);

      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).type("text/plain")
          .entity("Request failed! Check server logs for more information.").build();
    }

    return response;
  }
}
