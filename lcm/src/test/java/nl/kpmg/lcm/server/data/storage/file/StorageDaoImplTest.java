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
package nl.kpmg.lcm.server.data.storage.file;

import nl.kpmg.lcm.server.data.dao.file.StorageDaoImpl;
import java.util.HashMap;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.Storage;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
public class StorageDaoImplTest extends LCMBaseTest {

    @Autowired
    private StorageDaoImpl backendDao;

    @Test
    public void testPersist()  {
        Storage backendmodel = new Storage();
        backendmodel.setId("test");
        backendmodel.setOptions(new HashMap());
        backendmodel.getOptions().put("storagePath", "/tmp/");

        backendDao.persist(backendmodel);

        Storage byName = backendDao.getById("test");
        String path = (String) byName.getOptions().get("storagePath");
        assertEquals("/tmp/", path);

        assertEquals("test", byName.getId());
        assertEquals("/tmp/", (String) byName.getOptions().get("storagePath"));

    }
}
