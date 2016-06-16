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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Link;
import nl.kpmg.lcm.server.LinksDeserializer;
import nl.kpmg.lcm.server.LinksSerializer;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.rest.client.version0.UserController;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

/**
 * A wrapper class for a UserDescription and its links.
 *
 * @author mhoekstra
 */
public class UsersRepresentation {

    /**
     * The actual UserDescription.
     */
    private List<UserRepresentation> items;

    /**
     * The links the of a UserDescription.
     */
    @InjectLinks({
        @InjectLink(
            resource = UserController.class,
            style = InjectLink.Style.ABSOLUTE,
            rel = "user.overview"
        ),
        @InjectLink(
            resource = UserController.class,
            method = "createNewUser",
            style = InjectLink.Style.ABSOLUTE,
            rel = "user.create",
            type = "application/nl.kpmg.lcm.server.data.User+json"
        )
    })
    private List<Link> links;

    public UsersRepresentation() {
    }

    /**
     * @param items to wrap
     */
    public UsersRepresentation(final List<User> items) {
        this.items = new LinkedList();

        for (User item : items) {
            this.items.add(new UserRepresentation(item));
        }
    }


    /**
     * @return the UserDescription
     */
    public final List<UserRepresentation> getItems() {
        return items;
    }

    /**
     * @return the list of Links
     */
    @JsonSerialize(using = LinksSerializer.class)
    public final List<Link> getLinks() {
        return links;
    }
    
    @JsonDeserialize(using = LinksDeserializer.class)
    public final void setLinks(List<Link> links) {
        this.links = links;
    }
}
