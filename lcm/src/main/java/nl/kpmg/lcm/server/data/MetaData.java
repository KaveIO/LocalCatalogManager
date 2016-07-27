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
package nl.kpmg.lcm.server.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MetaData model class.
 *
 * The MetaData model acts as a decorator around an innerMap. A great deal of
 * freedom is required for handling metadata. The fields which are required for
 * correct LCM functioning are considered to be too limited to provide for
 * accurate and complete data descriptions. By decorating a map we should be
 * by quite metadata model agnostic. Any attributes we process in the LCM code
 * we should convert to hard attributes.
 *
 * @TODO Move the duplicates business logic to a service layer.
 */
@Document(collection = "metadata")
public class MetaData extends AbstractModel {

    /**
     * The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MetaData.class.getName());

    /**
     * The unique name of the metadata object.
     */
    private String name;

    /**
     * The inner map in which all the unknown attributes are stored.
     */
    private final Map<String, Object> innerMap;

    /**
     * Default constructor.
     */
    public MetaData() {
        this.innerMap = new HashMap();
    }

    /**
     * Constructor that filles the inner map with the given map.
     *
     * @param map to use as the innerMap
     */
    public MetaData(final Map map) {
        this.innerMap = new HashMap(map);
    }

    /**
     * Setter for values within the innerMap.
     *
     * This method is used by Jackson to fill values for which Jackson can't find
     * a proper attribute. The @JsonAnySetter annotation is used to enforce this.
     *
     * @param name of the property to set
     * @param value of the property to set
     */
    @JsonAnySetter
    public final void anySetter(final String name, final Object value) {
        innerMap.put(name, value);
    }

    /**
     * Getter for values within the innerMap.
     *
     * This method is used by Jackson to get values for which Jackson can't find
     * a proper attribute. The @JsonAnyGetter annotation is used to enforce this.
     *
     * @return the innerMap
     */
    @JsonAnyGetter
    public final Map anyGetter() {
        return innerMap;
    }

    public final <T> T get(final String path) {
        try {
            String[] split = path.split("\\.");
            return get(innerMap, split);
        } catch (Exception ex) {
            Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, "Couldn't find path: " + path, ex);
            return null;
        }

    }

    private final <T> T get(Map map, String[] path) {
        if (path.length == 0) {
            return null;
        } else if (map.containsKey(path[0])) {
            Object value = map.get(path[0]);
            if (path.length == 1) {
                return (T) value;
            } else {
                return get((Map) value, (String[]) ArrayUtils.removeElement(path, path[0]));
            }
        } else {
            return null;
        }
    }

    public final void set(final String path, final Object value) {
        try {
            String[] split = path.split("\\.");
            set(innerMap, split, value);
        } catch (Exception ex) {
            Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, "Couldn't find path: " + path, ex);
        }

    }

    private void set(final Map map, final String[] path, final Object value) throws Exception {
        if (path.length == 0) {
            throw new Exception("Errrrr");
        } else if (path.length == 1) {
            map.put(path[0], value);
        } else if (map.containsKey(path[0])) {
            Class<? extends Object> elementClass = map.get(path[0]).getClass();
            if (Map.class.isAssignableFrom(elementClass)) {
                set((Map) map.get(path[0]), (String[]) ArrayUtils.removeElement(path, path[0]), value);
            } else {
                throw new Exception("Trying to traverse deeper but this path element isn't a map");
            }
        } else {
            HashMap newMap = new HashMap();
            map.put(path[0], newMap);
            set(newMap, (String[]) ArrayUtils.removeElement(path, path[0]), value);
        }
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final String getDataUri() {
        return get("data.uri");
    }

    public final void setDataUri(final String dataUri) {
        set("data.uri", dataUri);
    }

    public void setDuplicates(List<MetaData> duplicates) {
        this.set("duplicates", duplicates);
    }

    public void addDuplicate(MetaData duplicate) {
        List<MetaData> lmdata = new LinkedList();
        if (getDuplicates() != null) {
            lmdata = this.getDuplicates();
            lmdata.add(duplicate);
            this.setDuplicates(lmdata);
        } else {
            lmdata.add(duplicate);
            this.setDuplicates(lmdata);

        }
    }

    public List<MetaData> getDuplicates() {
        List<MetaData> lmdata = new LinkedList();
        if (innerMap.containsKey("duplicates")) {
            List nestedMetaData = this.get("duplicates");

            for (Object duplicate : nestedMetaData) {

                if (MetaData.class.isAssignableFrom(duplicate.getClass())) {
                    lmdata.add((MetaData) duplicate);
                } else if (Map.class.isAssignableFrom(duplicate.getClass())) {
                    lmdata.add(new MetaData((Map) duplicate));
                } else {
                    LOGGER.log(Level.WARNING, "Error while constructing duplicates list for MetaData: {0}", this.getName());
                }
            }
            return lmdata;
        } else {
            return null;
        }
    }
}
