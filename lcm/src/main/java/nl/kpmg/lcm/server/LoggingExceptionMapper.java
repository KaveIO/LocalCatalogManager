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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Catch all exception mapper for the REST interface.
 *
 * By default uncaught exceptions are not displayed by grizzly. This is nice
 * but terrible for debugging. This mapper makes sure all uncaught exceptions
 * are at least logged as a warning. This has a drawback that in some cases
 * default behavior is lost (404 messages).
 *
 * @author mhoekstra
 */
@Provider
public class LoggingExceptionMapper  implements ExceptionMapper<Exception> {
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(LoggingExceptionMapper.class.getName());

    /**
     * Translates an exception to a response.
     *
     * @param ex the caught exception
     * @return the response to provide.
     */
    @Override
    public final Response toResponse(final Exception ex) {
        Response response;
        if (NotFoundException.class.isAssignableFrom(ex.getClass())) {
            response = Response.status(Response.Status.NOT_FOUND)
                    .type("text/plain")
                    .build();
        } else {
            LOGGER.log(Level.WARNING, "Request failed", ex);
            response = Response.serverError()
                    .entity("Request failed")
                    .type("text/plain")
                    .build();
        }

        return response;
    }
}