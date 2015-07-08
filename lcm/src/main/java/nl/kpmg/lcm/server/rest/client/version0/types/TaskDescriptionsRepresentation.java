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
package nl.kpmg.lcm.server.rest.client.version0.types;

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Link;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.rest.client.version0.TaskDescriptionController;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

/**
 * A wrapper class for TaskDescriptions and their links.
 *
 * @author mhoekstra
 */
public class TaskDescriptionsRepresentation {

    /**
     * The list of actual TaskDescriptions.
     */
    private final List<TaskDescriptionRepresentation> items;

    /**
     * The links the of a TaskDescription.
     */
    @InjectLinks({
        @InjectLink(
            resource = TaskDescriptionController.class,
            style = Style.ABSOLUTE,
            rel = "tasks.overview"
        ),
        @InjectLink(
            resource = TaskDescriptionController.class,
            style = Style.ABSOLUTE,
            rel = "tasks.create",
            type = "application/nl.kpmg.lcm.TaskDescription+json"
        )
    })
    private List<Link> links;

    /**
     * @param items to wrap
     */
    public TaskDescriptionsRepresentation(final List<TaskDescription> items) {
        this.items = new LinkedList();

        for (TaskDescription item : items) {
            this.items.add(new TaskDescriptionRepresentation(item));
        }
    }

    /**
     * @return the wrapped items
     */
    public final List<TaskDescriptionRepresentation> getItems() {
        return items;
    }

    /**
     * @return the list of links
     */
    public final List<Link> getLinks() {
        return links;
    }
}


