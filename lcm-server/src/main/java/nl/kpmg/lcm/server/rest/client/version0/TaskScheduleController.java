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

import nl.kpmg.lcm.common.rest.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.common.data.TaskSchedule;
import nl.kpmg.lcm.server.data.service.TaskScheduleService;
import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteTaskScheduleRepresentation;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/taskschedule")
public class TaskScheduleController {

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
  public final TaskScheduleRepresentation getCurrent() {
    TaskSchedule taskSchedule = taskScheduleService.findFirstByOrderByIdDesc();

    ConcreteTaskScheduleRepresentation concreteTaskScheduleRepresentation =
        new ConcreteTaskScheduleRepresentation(taskSchedule);
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
  public final Response createTaskSchedule(final TaskSchedule taskSchedule) {
    taskSchedule.setId(null);
    taskScheduleService.save(taskSchedule);
    return Response.ok().build();
  }
}
