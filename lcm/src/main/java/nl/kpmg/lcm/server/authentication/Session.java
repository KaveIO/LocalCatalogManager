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

import java.util.Date;

/**
 * @author mhoekstra
 */
public final class Session {
    /**
     * The name of the logged in user.
     */
    private final String username;

    /**
     * The role of the logged in user.
     *
     * {@note Currently users can only have a single role since the role based
     * authentication is pretty basic. Perhaps in the future we'll support
     * multiple roles per user.}
     */
    private final String role;

    /**
     * The origin mechanism which caused the user authentication to succeed.
     */
    private final UserOrigin userOrigin;

    /**
     * The date when this session object was constructed.
     *
     * @note should be used to invalidate or clean sessions
     */
    private final Date loginSince;

    /**
     * A modifiable date marking the last time this Session was used.
     *
     * @note should be used to invalidate or clean sessions
     */
    private Date lastSeen;

    /**
     * Default constructor.
     *
     * @param username of the user owning this session
     * @param role of the user owning this session
     * @param userOrigin which caused this session to be created
     */
    public Session(final String username, final String role, final UserOrigin userOrigin) {
        this.username = username;
        this.role = role;
        this.userOrigin = userOrigin;
        this.loginSince = new Date();
        this.lastSeen = new Date();
    }

    /**
     * Overwrites the current lastSeen timestamp with the current time.
     */
    public void updateLastSeen() {
        this.lastSeen = new Date();
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @return the origin mechanism of the session
     */
    public UserOrigin getUserOrigin() {
        return userOrigin;
    }

    /**
     * @return the date when this Session was constructed
     */
    public Date getLoginSince() {
        return loginSince;
    }

    /**
     * @return the date when this Session was last used
     */
    public Date getLastSeen() {
        return lastSeen;
    }
}
