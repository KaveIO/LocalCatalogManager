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
package nl.kpmg.lcm.server.exception.mapper;

import nl.kpmg.lcm.server.exception.LcmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 *
 * @author shristov
 */
public class LcmExceptionMapper implements ExceptionMapper<LcmException> {
  /**
   * The logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LcmExceptionMapper.class);

  @Override
  public Response toResponse(LcmException exception) {
    LOGGER.warn("Lcm Exception is thrown with messages: " + exception.getMessage());
    Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
    if (exception.getResponseCode() != null) {
      status = exception.getResponseCode();
    }
    return Response.status(status).type("text/plain")
        .entity(exception.getMessage()).build();
  }

}
