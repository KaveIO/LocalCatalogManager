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
 * Enumeration like class describing the user roles in the system.
 *
 * The controller endpoints are protected by RolesAllowed annotations. These
 * strings control who can access a resource and who can't. In order to avoid
 * mistakes this class restricts the possibilities. Normally a Enumeration
 * would be appropriate here however Enumerations can't be used as strings in
 * annotation arguments which renders that route impossible.
 *
 * @author mhoekstra
 */
public final class Roles {
    /**
     * Role string for a core user that is allow to do everything.
     */
    public static final String ADMINISTRATOR = "administrator";

    /**
     * Role string for a low level privileged user that can do standard API operations.
     */
    public static final String API_USER = "apiUser";

    /**
     * Private default constructor to disable object construction.
     */
    private Roles() {  }
}
