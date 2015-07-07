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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author mhoekstra
 */
public class MetaData extends HashMap {

    public <T> T get(String path) {
        try {
            String[] split = path.split("\\.");
            return get(this, split);
        } catch (Exception ex) {
            Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, "Couldn't find path: " + path, ex);
            return null;
        }

    }

    private <T> T get(Map map, String[] path) throws Exception {
        if (path.length == 0) {
            throw new Exception("Errrrr");
        } else if (map.containsKey(path[0])) {
            Object value = map.get(path[0]);
            if (path.length == 1) {
                return (T) value;
            } else {
                return get((Map) value, (String[]) ArrayUtils.removeElement(path, path[0]));
            }
        } else {
            throw new Exception("Errrrr");
        }
    }

    public void set(String path, Object value) {
        try {
            String[] split = path.split("\\.");
            set(this, split, value);
        } catch (Exception ex) {
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

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public String getVersionNumber() {
        return get("version.number");
    }

    public void setVersionNumber(String versionNumber) {
        set("version.number", versionNumber);
    }

    public String getDataUri() {
        return get("data.uri");
    }

    public void setDataUri(String dataUri) {
        set("data.uri", dataUri);
    }
}
