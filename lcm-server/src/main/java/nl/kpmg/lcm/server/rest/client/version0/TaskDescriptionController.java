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
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.server.data.service.TaskDescriptionService;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteTaskDescriptionRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteTaskDescriptionsRepresentation;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/tasks")
@Api(value = "v0 task descriptions")
public class TaskDescriptionController {

  private final TaskDescriptionService taskDescriptionService;

  @Autowired
  public TaskDescriptionController(final TaskDescriptionService taskDescriptionService) {
    this.taskDescriptionService = taskDescriptionService;
  }

  /**
   * Get a list of the tasks that meets the specified criteria.
   *
   * @param status to filter the tasks on
     * @param type
   * @return a list of all tasks
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.TaskDescriptionsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Get task descriptions filter by specified parameters."
          + " The results are ordered desending by start time.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final TaskDescriptionsRepresentation getOverview(
      @ApiParam( value = "Task description status.", allowableValues = "PENDING, SCHEDULED, RUNNING, FAILED, SUCCESS")
      @QueryParam("status") final TaskDescription.TaskStatus status,
      @ApiParam( value = "Task description type.", allowableValues = "FETCH, ENRICHMENT, ENRICHMENT_MANAGER") 
      @QueryParam("type") final TaskType type, 
      @ApiParam( value = "Maximum returned items.") 
      @QueryParam("limit") final Integer limit) {

    List taskDescriptions = null;
    if (status != null && type != null) {
      if (limit != null) {
        taskDescriptions = taskDescriptionService.findByTypeAndStatus(type, status, limit);
      } else {
        taskDescriptions = taskDescriptionService.findByTypeAndStatus(type, status);
      }
    } else if (status != null) {
      if (limit != null) {
        taskDescriptions = taskDescriptionService.findByStatus(status, limit);
      } else {
        taskDescriptions = taskDescriptionService.findByStatus(status, limit);
      }
    } else if (type != null) {
      if (limit != null) {
        taskDescriptions = taskDescriptionService.findByType(type, limit);
      } else {
        taskDescriptions = taskDescriptionService.findByType(type);
      }
    } else if (limit != null) {
      taskDescriptions = taskDescriptionService.find(limit);
    } else {
      throw new LcmException("Wrong parameters take a look at the documentation!");
    }

    ConcreteTaskDescriptionsRepresentation concreteTaskDescriptionsRepresentation =
        new ConcreteTaskDescriptionsRepresentation();
    if (taskDescriptions != null) {
      concreteTaskDescriptionsRepresentation.setRepresentedItems(
          ConcreteTaskDescriptionRepresentation.class, taskDescriptions);
    }
    return concreteTaskDescriptionsRepresentation;
  }

  /**
   * Get information about a specific tasks.
   *
   * @param taskDescription the task description
   * @return 200 OK if successful
   */
  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.TaskDescription+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Create task description.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response createTask(
          @ApiParam( value = "Task description object.")
          final TaskDescription taskDescription) {
    taskDescription.setId(null);
    taskDescription.setStatus(TaskDescription.TaskStatus.PENDING);
    taskDescription.setOutput(null);
    taskDescription.setStartTime(null);
    taskDescription.setEndTime(null);

    taskDescriptionService.createNew(taskDescription);

    return Response.ok().build();
  }

  /**
   * Get information about a specific tasks.
   *
   * @param taskDescriptionId the id of the task
   * @return the TaskDescription
   */
  @GET
  @Path("{tasks_id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.TaskDescriptionRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Get task description.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "Task description is not found")})
  public final Response getTask(
          @ApiParam( value = "Task description id.")
          @PathParam("tasks_id") final String taskDescriptionId) {
    TaskDescription taskDescriptions =
        taskDescriptionService.findOne(taskDescriptionId);

    if (taskDescriptions != null) {
      return Response.ok(new ConcreteTaskDescriptionRepresentation(taskDescriptions)).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  /**
   * Delete a tasks.
   *
   * @param taskDescriptionId the id of the task
   * @return 200 OK if successful
   */
  @DELETE
  @Path("{tasks_id}")
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Delete task description.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "Task description is not found")})
  public final Response deleteCommand(
          @ApiParam( value = "Task description id.")
          @PathParam("tasks_id") final String taskDescriptionId) {
    TaskDescription taskDescriptions =
        taskDescriptionService.findOne(taskDescriptionId);

    if (taskDescriptions != null) {
      taskDescriptionService.delete(taskDescriptionId);
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}
