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

import nl.kpmg.lcm.common.rest.types.TaskDescriptionStatusFilter;
import nl.kpmg.lcm.common.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.server.data.service.TaskDescriptionService;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteTaskDescriptionRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteTaskDescriptionsRepresentation;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/tasks")
public class TaskDescriptionController {

  private final TaskDescriptionService taskDescriptionService;

  @Autowired
  public TaskDescriptionController(final TaskDescriptionService taskDescriptionService) {
    this.taskDescriptionService = taskDescriptionService;
  }

  /**
   * Get a list of all the tasks.
   *
   * @param status to filter the tasks on
   * @return a list of all tasks
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.TaskDescriptionsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final TaskDescriptionsRepresentation getOverview(
      @DefaultValue("ALL") @QueryParam("status") final TaskDescriptionStatusFilter status) {

    List taskDescriptions;
    switch (status) {
      case PENDING:
        taskDescriptions = taskDescriptionService
            .findByStatus(TaskDescription.TaskStatus.PENDING);
        break;
      case RUNNING:
        taskDescriptions = taskDescriptionService
            .findByStatus(TaskDescription.TaskStatus.RUNNING);
        break;
      case SCHEDULED:
        taskDescriptions = taskDescriptionService
            .findByStatus(TaskDescription.TaskStatus.SCHEDULED);
        break;
      case SUCCESS:
        taskDescriptions = taskDescriptionService
            .findByStatus(TaskDescription.TaskStatus.SUCCESS);
        break;
      case FAILED:
        taskDescriptions = taskDescriptionService
            .findByStatus(TaskDescription.TaskStatus.FAILED);
        break;
      case ALL:
      default:
        taskDescriptions = taskDescriptionService.findAll();
        break;
    }


    ConcreteTaskDescriptionsRepresentation concreteTaskDescriptionsRepresentation =
        new ConcreteTaskDescriptionsRepresentation();
    if (taskDescriptions != null) {
      concreteTaskDescriptionsRepresentation
          .setRepresentedItems(ConcreteTaskDescriptionRepresentation.class, taskDescriptions);
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
  public final Response createTask(final TaskDescription taskDescription) {
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
  public final Response getTask(@PathParam("tasks_id") final String taskDescriptionId) {
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
  public final Response deleteCommand(@PathParam("tasks_id") final String taskDescriptionId) {
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
