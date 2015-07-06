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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.commons.lang.NotImplementedException;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/tasks")
public class Tasks {

    /**
     * Get a list of all the tasks running
     *
     * @return
     */
    @GET
    @Produces({"application/json"})
    public String getCommands() {
        throw new NotImplementedException();
    }

    /**
     * Get information about a specific tasks
     *
     * @param tasksId
     * @return
     */
    @GET
    @Path("{tasks_id}")
    @Produces({"application/json"})
    public String getCommand(
            @PathParam("tasks_id") String tasksId) {
        throw new NotImplementedException();
    }

    /**
     * Unschedule a tasks
     *
     * @param tasksId
     * @return
     */
    @DELETE
    @Path("{tasks_id}")
    @Produces({"application/json"})
    public String deleteCommand(
            @PathParam("tasks_id") String tasksId) {
        throw new NotImplementedException();
    }
}
