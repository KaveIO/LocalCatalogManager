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

/**
 *
 * @author mhoekstra
 */
public class MetaData {

    private static final Logger LOGGER = Logger.getLogger(MetaData.class.getName());

    private final Map<String, Object> innerMap;

    public MetaData() {
        this.innerMap = new HashMap();
    }

    public MetaData(final Map map) {
        this.innerMap = new HashMap(map);
    }

    @JsonAnySetter
    @PermitAll
    public void anySetter(String name, Object value) {
        innerMap.put(name, value);
    }

    @JsonAnyGetter
    @PermitAll
    public Map anyGetter() {
        return innerMap;
    }

    public <T> T get(String path) {
        try {
            String[] split = path.split("\\.");
            return get(innerMap, split);
        }
        catch (Exception ex) {
            Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, "Couldn't find path: " + path, ex);
            return null;
        }

    }

    private <T> T get(Map map, String[] path) {
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

    public void set(String path, Object value) {
        try {
            String[] split = path.split("\\.");
            set(innerMap, split, value);
        }
        catch (Exception ex) {
            Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, "Couldn't find path: " + path, ex);
        }

    }

    private void set(Map map, String[] path, Object value) throws Exception {
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

    @JsonIgnore
    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    @JsonIgnore
    public String getVersionNumber() {
        return get("version.number");
    }

    public void setVersionNumber(String versionNumber) {
        set("version.number", versionNumber);
    }

    @JsonIgnore
    public String getDataUri() {
        return get("data.uri");
    }

    public void setDataUri(String dataUri) {
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

    @JsonIgnore
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
