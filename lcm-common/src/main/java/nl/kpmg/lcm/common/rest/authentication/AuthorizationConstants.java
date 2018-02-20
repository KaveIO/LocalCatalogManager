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
package nl.kpmg.lcm.common.rest.authentication;

/**
 *
 * @author shristov
 */
public class AuthorizationConstants {
  /**
   * The name of the http request header containing the authentication user.
   */
  public static final String LCM_AUTHENTICATION_USER_HEADER = "LCM-Authentication-User";

  /**
   * The name of the http request header containing the lcm unique id which identifies user's origin.
   */
  public static final String LCM_AUTHENTICATION_ORIGIN_HEADER = "LCM-Authentication-origin";

  /**
   * The name of the http request header containing the username of the actual user logged on the remote LCM.
   */
  public static final String LCM_AUTHENTICATION_REMOTE_USER_HEADER = "LCM-Authentication-remote-user";

  /**
   * The name of the http request header containing the authentication token.
   */
  public static final String LCM_AUTHENTICATION_TOKEN_HEADER = "LCM-Authentication-Token";

    /**
   * The name of the http request header containing the authentication user.
   */
  public static final String BASIC_AUTHENTICATION_HEADER = "Authorization";

}
