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

package nl.kpmg.lcm.server.rest.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Filter placed on all REST responses that modifies the headers sent to the client.
 *
 * @author mhoekstra
 */
@Provider
@PreMatching
public class ResponseFilter implements ContainerResponseFilter {

  /**
   * The class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFilter.class.getName());

  /**
   * Filters the responses and adds the appropriate authentication headers.
   *
   * @param requestContext the request
   * @param responseContext the response
   */
  @Override
  public final void filter(final ContainerRequestContext requestContext,
      final ContainerResponseContext responseContext) {
    LOGGER.trace(String.format("LCMRESTResponseFilter called with Entity %s",
        responseContext.getEntity()));

    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    headers.add("Acces-Control-Allow-Headers",
        String.format("%s, %s", SessionAuthenticationManager.LCM_AUTHENTICATION_USER_HEADER,
            SessionAuthenticationManager.LCM_AUTHENTICATION_TOKEN_HEADER));
    headers.add("WWW-Authenticate", "Basic realm=\"LCM\", LCMToken realm=\"LCM\"");
  }
}
