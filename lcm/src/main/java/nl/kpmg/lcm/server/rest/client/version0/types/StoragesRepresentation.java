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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Link;
import nl.kpmg.lcm.server.LinksSerializer;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.rest.client.version0.StorageController;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

/**
 * A wrapper class for a TaskDescription and its links.
 *
 * @author mhoekstra
 */
public class StoragesRepresentation {

    /**
     * The actual TaskDescription.
     */
    private final List<StorageRepresentation> items;

    /**
     * The links the of a TaskDescription.
     */
    @InjectLinks({
        @InjectLink(
            resource = StorageController.class,
            style = InjectLink.Style.ABSOLUTE,
            rel = "storage.overview"
        ),
        @InjectLink(
            resource = StorageController.class,
            method = "createNewStorage",
            style = InjectLink.Style.ABSOLUTE,
            rel = "storage.create",
            type = "application/nl.kpmg.lcm.server.data.Backend+json"
        )
    })
    private List<Link> links;

    /**
     * @param items to wrap
     */
    public StoragesRepresentation(final List<Storage> items) {
        this.items = new LinkedList();

        for (Storage item : items) {
            this.items.add(new StorageRepresentation(item));
        }
    }


    /**
     * @return the TaskDescription
     */
    public final List<StorageRepresentation> getItems() {
        return items;
    }

    /**
     * @return the list of Links
     */
    @JsonSerialize(using = LinksSerializer.class)
    public final List<Link> getLinks() {
        return links;
    }
}
