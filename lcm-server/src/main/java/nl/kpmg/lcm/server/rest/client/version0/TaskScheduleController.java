/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.rest.client.version0;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.TaskSchedule;
import nl.kpmg.lcm.common.rest.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.server.data.service.TaskScheduleService;
import nl.kpmg.lcm.server.rest.UserIdentifier;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteTaskScheduleRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/taskschedule")
@Api(value = "v0 taskschedule(cron jobs)")
public class TaskScheduleController {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  /**
   * The TaskDescription DAO.
   */
  @Autowired
  private TaskScheduleService taskScheduleService;

  /**
   * @return a list of all tasks
   */
  @GET
  @Produces({"application/json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Return the active schedule with all active cron jobs", 
          notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final TaskScheduleRepresentation getCurrent(@Context SecurityContext securityContext) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access the current task schedule.");

    TaskSchedule taskSchedule = taskScheduleService.findFirstByOrderByIdDesc();

    ConcreteTaskScheduleRepresentation concreteTaskScheduleRepresentation =
        new ConcreteTaskScheduleRepresentation(taskSchedule);

    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " accessed successfully the current task schedule.");
    return concreteTaskScheduleRepresentation;
  }

  /**
   * create a new TaskSchedule.
   *
   * @param taskSchedule the task schedule
   * @return 200 OK if successful
   */
  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.TaskSchedule+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
    @ApiOperation(value = "Create the active schedule", 
          notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response createTaskSchedule(@Context SecurityContext securityContext, @ApiParam(
      value = "TaskSchedule object.") final TaskSchedule taskSchedule) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to create a new task schedule.");

    taskSchedule.setId(null);
    TaskSchedule newTaskSchedule = taskScheduleService.save(taskSchedule);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " created successfully new task schedule with id : " + newTaskSchedule.getId() + ".");
    return Response.ok().build();
  }
}
