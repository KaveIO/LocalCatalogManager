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
package nl.kpmg.lcm.server.authentication;

/**
 * Enumeration used to mark the method used to authenticate a user.
 *
 * @author mhoekstra
 */
 public enum UserOrigin {

    /**
     * Marks the user as the administrator provided by an external configuration.
     */
    CONFIGURED,

    /**
     * Marks an user as coming from the local object data store (file or Mongo).
     */
    LOCAL,

    /**
     * Mark an user as coming from a connected LDAP server.
     */
    LDAP;
}
