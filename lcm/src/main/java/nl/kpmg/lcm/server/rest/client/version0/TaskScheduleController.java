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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskScheduleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

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
    private TaskScheduleDao taskScheduleDao;

    /**
     * @return a list of all tasks
     */
    @GET
    @Produces({"application/json" })
    public final TaskScheduleRepresentation getCurrent() {
        TaskSchedule taskSchedule = taskScheduleDao.getCurrent();

        if (taskSchedule != null) {
            return new TaskScheduleRepresentation(taskSchedule);
        }
        return new TaskScheduleRepresentation();
    }

    /**
     * create a new TaskSchedule.
     *
     * @param taskSchedule the task schedule
     * @return 200 OK if successful
     */
    @POST
    @Consumes({"application/x-nl.kpmg.lcm.server.data.TaskSchedule+json" })
    public final Response createTaskSchedule(final TaskSchedule taskSchedule) {
        taskSchedule.setId(null);
        taskScheduleDao.persist(taskSchedule);
        return Response.ok().build();
    }
}
