/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.server.rest;

import nl.kpmg.lcm.common.data.User;

import org.springframework.stereotype.Component;

import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author shristov
 */
@Component
public class UserIdentifier {

  public String getUserDescription(SecurityContext securityContext, boolean isRequestCompleted) {
    if (!isRequestCompleted) {
      return "User: " + getUserName(securityContext) + " with role: "
          + getUserRole(securityContext) + " and origin: " + getUserOrigin(securityContext);
    }
    return "User: " + getUserName(securityContext) + " with origin: "
        + getUserOrigin(securityContext);
  }

  private String getUserName(SecurityContext securityContext) {
    User user = (User) securityContext.getUserPrincipal();
    return user.getName();
  }

  private String getUserRole(SecurityContext securityContext) {
    User user = (User) securityContext.getUserPrincipal();
    return user.getRole();
  }

  private String getUserOrigin(SecurityContext securityContext) {
    User user = (User) securityContext.getUserPrincipal();
    return user.getOrigin();
  }
}
