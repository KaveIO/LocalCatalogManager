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
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.rest.client.version0.LocalMetaDataController;
import nl.kpmg.lcm.server.rest.client.version0.TaskDescriptionController;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

/**
 * A wrapper class for a TaskDescription and its links.
 *
 * @author mhoekstra
 */
public class MetaDatasRepresentation {

    /**
     * The actual TaskDescription.
     */
    private final List<MetaDataRepresentation> items;

    /**
     * The links the of a TaskDescription.
     */
    @InjectLinks({
        @InjectLink(
            resource = LocalMetaDataController.class,
            style = InjectLink.Style.ABSOLUTE,
            rel = "local.metadata.overview"
        ),
        @InjectLink(
            resource = LocalMetaDataController.class,
            method = "createNewMetaData",
            style = InjectLink.Style.ABSOLUTE,
            rel = "local.metadata.create",
            type = "application/nl.kpmg.lcm.server.data.MetaData+json"
        )
    })
    private List<Link> links;

    /**
     * @param items to wrap
     */
    public MetaDatasRepresentation(final List<MetaData> items) {
        this.items = new LinkedList();

        for (MetaData item : items) {
            this.items.add(new MetaDataRepresentation(item));
        }
    }


    /**
     * @return the TaskDescription
     */
    public final List<MetaDataRepresentation> getItems() {
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
