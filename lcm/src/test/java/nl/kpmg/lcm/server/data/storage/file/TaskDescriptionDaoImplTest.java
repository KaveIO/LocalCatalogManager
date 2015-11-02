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

import nl.kpmg.lcm.server.data.dao.file.TaskDescriptionDaoImpl;
import nl.kpmg.lcm.server.data.dao.DaoException;
import java.io.File;
import java.util.Date;
import java.util.List;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.dao.file.ObjectMapperFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
public class TaskDescriptionDaoImplTest extends LCMBaseTest {

    @Autowired
    private TaskDescriptionDaoImpl taskDescriptionDao;

    @Test
    public void testPersistWithANewObject() {
        TaskDescription taskDescription = new TaskDescription();

        taskDescription.setId("testname");
        taskDescription.setJob("test job");
        taskDescription.setTarget("test target");
        taskDescription.setOutput("test output");
        taskDescription.setStartTime(new Date());
        taskDescription.setEndTime(new Date());
        taskDescription.setStatus(TaskDescription.TaskStatus.PENDING);

        taskDescriptionDao.persist(taskDescription);

        TaskDescription actual = taskDescriptionDao.getById("testname");

        // We didn't write a nice mapper for this object so Check if all fields
        // got presisted nicely.
        assertEquals(taskDescription.getId(), actual.getId());
        assertEquals(taskDescription.getJob(), actual.getJob());
        assertEquals(taskDescription.getTarget(), actual.getTarget());
        assertEquals(taskDescription.getOutput(), actual.getOutput());
        assertEquals(taskDescription.getStartTime(), actual.getStartTime());
        assertEquals(taskDescription.getEndTime(), actual.getEndTime());
        assertEquals(taskDescription.getStatus(), actual.getStatus());
    }

    @Test
    public void testPersistWithANewObjectShouldSetTheIdOnTheLocalObject() {
        TaskDescription taskDescription = new TaskDescription();
        taskDescriptionDao.persist(taskDescription);

        assertNotNull(taskDescription.getId());
    }

    @Test
    public void testGetAllReturnsMultipleTaskDescriptions() {
        TaskDescription taskDescription;

        taskDescription = new TaskDescription();
        taskDescription.setId("test1");
        taskDescriptionDao.persist(taskDescription);

        taskDescription = new TaskDescription();
        taskDescription.setId("test2");
        taskDescriptionDao.persist(taskDescription);

        // Fetch all the task descriptions
        List<TaskDescription> all = taskDescriptionDao.getAll();

        assertTrue(all.size() == 2);
    }
}
