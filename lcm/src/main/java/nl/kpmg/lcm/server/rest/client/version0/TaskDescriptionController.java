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
package nl.kpmg.lcm.server.rest.client.version0;

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskDescriptionRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskDescriptionStatusFilter;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskDescriptionsRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/tasks")
public class TaskDescriptionController {

    /**
     * The TaskDescription DAO.
     */
    @Autowired
    private TaskDescriptionDao taskDescriptionDao;

    /**
     * Get a list of all the tasks.
     *
     * @param status to filter the tasks on
     * @return a list of all tasks
     */
    @GET
    @Produces({"application/json" })
    public final TaskDescriptionsRepresentation getOverview(
            @DefaultValue("ALL") @QueryParam("status") final TaskDescriptionStatusFilter status) {

        List<TaskDescription> taskDescriptions;
        switch(status) {
            case PENDING:
                taskDescriptions = taskDescriptionDao.getByStatus(TaskDescription.TaskStatus.PENDING);
                break;
            case RUNNING:
                taskDescriptions = taskDescriptionDao.getByStatus(TaskDescription.TaskStatus.RUNNING);
                break;
            case SCHEDULED:
                taskDescriptions = taskDescriptionDao.getByStatus(TaskDescription.TaskStatus.SCHEDULED);
                break;
            case SUCCESS:
                taskDescriptions = taskDescriptionDao.getByStatus(TaskDescription.TaskStatus.SUCCESS);
                break;
            case FAILED:
                taskDescriptions = taskDescriptionDao.getByStatus(TaskDescription.TaskStatus.FAILED);
                break;
            case ALL:
            default:
                taskDescriptions = taskDescriptionDao.getAll();
                break;
        }

        if (taskDescriptions != null) {
            return new TaskDescriptionsRepresentation(taskDescriptions);
        }
        return new TaskDescriptionsRepresentation(new LinkedList());
    }

    /**
     * Get information about a specific tasks.
     *
     * @param taskDescription the task description
     * @return 200 OK if successful
     */
    @POST
    @Consumes({"application/x-nl.kpmg.lcm.server.data.TaskDescription+json" })
    public final Response createTask(final TaskDescription taskDescription) {
        taskDescription.setId(null);
        taskDescription.setStatus(TaskDescription.TaskStatus.PENDING);
        taskDescription.setOutput(null);
        taskDescription.setStartTime(null);
        taskDescription.setEndTime(null);

        taskDescriptionDao.persist(taskDescription);

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
    @Produces({"application/json" })
    public final TaskDescriptionRepresentation getTask(@PathParam("tasks_id") final Integer taskDescriptionId) {
        TaskDescription taskDescriptions = taskDescriptionDao.getById(taskDescriptionId);

        if (taskDescriptions != null) {
            return new TaskDescriptionRepresentation(taskDescriptions);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    /**
     * Delete a tasks.
     *
     * @param taskDescriptionId the id of the task
     * @return 200 OK if successful
     */
    @DELETE
    @Path("{tasks_id}")
    @Produces({"application/json" })
    public final Response deleteCommand(@PathParam("tasks_id") final Integer taskDescriptionId) {
        TaskDescription taskDescriptions = taskDescriptionDao.getById(taskDescriptionId);

        if (taskDescriptions != null) {
            taskDescriptionDao.delete(taskDescriptions);
            return Response.ok().build();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
