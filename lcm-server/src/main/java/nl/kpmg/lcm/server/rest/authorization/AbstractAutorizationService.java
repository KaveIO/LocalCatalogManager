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
package nl.kpmg.lcm.server.rest.authorization;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is designed to be super class of implementations of AuthorizationService. When you
 * extend it fill with data the authorizationMap in loadAuthorizationMap method.
 *
 * @author shristov
 */
public abstract class AbstractAutorizationService implements AuthorizationService {
  private final Logger logger = Logger.getLogger(AbstractAutorizationService.class.getName());

  private Map<String, List<String>> authorizationMap;

  public AbstractAutorizationService() {
    authorizationMap = loadAuthorizationMap();
  }

  @Override
  public boolean isAuthorized(String resourceId, String role) {
    if (authorizationMap == null) {
      logger.log(Level.SEVERE, "AuhotrizationService is used without initialization");
      return false;
    }

    List<String> permittedRoles = authorizationMap.get(resourceId);
    if(permittedRoles ==  null) {
        return false;
    }

    for (String permittedRole : permittedRoles) {
      if (role.equals(permittedRole)) {
        return true;
      }
    }

    return false;
  }
  /**
   * Load permission frome external permissions source
   * @return  map that contains the permission. The key is the resource name and the
   * values are all the permissions which have access to the resource
   */
  protected abstract Map<String, List<String>> loadAuthorizationMap();
}
