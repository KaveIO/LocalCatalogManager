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
package nl.kpmg.lcm.server.rest.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.ws.rs.container.ContainerRequestContext;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author mhoekstra
 */
public abstract class AuthenticationManager {
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthenticationManager.class);


    /**
     * The user service.
     */
    private final UserService userService;

    /**
     * the hard admin username provided by the properties file.
     */
    private String adminUser;

    /**
     * the hard admin password provided by the properties file.
     */
    private String adminPassword;

    public abstract boolean isEnabled();

    public abstract boolean isAuthenticationValid(ContainerRequestContext requestContext);

    public abstract UserSecurityContext getSecurityContext(ContainerRequestContext requestContext);

    @Autowired
    public AuthenticationManager(UserService userService) {
        this.userService = userService;
    }

    @Value("${lcm.server.adminUser}")
    public final void setAdminUser(final String adminUser) {
        this.adminUser = adminUser;
    }

    @Value("${lcm.server.adminPassword}")
    public final void setAdminPassword(final String adminPassword) {
        this.adminPassword = adminPassword;
    }

    protected boolean isUsernamePasswordValid(final String username, final String password) {
        if (username.equals(adminUser)) {
            LOGGER.info("Caught login attempt for admin user");
            if (password.equals(adminPassword)) {
                return true;
            }
        } else {
            LOGGER.info("Caught login attempt for regular user");
            User user = userService.getUserDao().getById(username);
            try {
                if (user != null && user.passwordEquals(password)) {
                    return true;
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                LOGGER.error("Something went wrong with the password hashing algorithm", ex);
            }
        }
        return false;
    }

    protected Session createSessionForUser(String username) throws LoginException {
        if (username.equals(adminUser)) {
            return new Session(username, Roles.ADMINISTRATOR, UserOrigin.CONFIGURED);
        } else {
            User user = userService.getUserDao().getById(username);
            if (user != null) {
                return new Session(user.getId(), user.getRole(), UserOrigin.LOCAL);
            }
        }
        throw new LoginException("Session could not be constructed after login.");
    }
}
