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

import nl.kpmg.lcm.common.GeneralExceptionMapper;
import nl.kpmg.lcm.common.exception.LcmValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 *
 * @author shristov
 */
public class ValidationExceptionMapper implements ExceptionMapper<LcmValidationException> {
  /**
   * The logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GeneralExceptionMapper.class);

  @Override
  public Response toResponse(LcmValidationException exception) {
    LOGGER.warn("Validation Exception is thrown with messages: "
        + exception.getNotification().errorMessage());

    List<Exception> exceptionList = exception.getNotification().getExceptionList();
    exceptionList.forEach((e) -> {
        LOGGER.debug("Exception: " + e.getClass().getName() + ". Message: " + e.getMessage());
      });

    return Response.status(Response.Status.BAD_REQUEST).type("text/plain")
        .entity(exception.getNotification().errorMessage()).build();

  }

}
