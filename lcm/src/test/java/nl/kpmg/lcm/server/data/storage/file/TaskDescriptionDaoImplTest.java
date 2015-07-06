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
import nl.kpmg.lcm.server.data.TaskDescription;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mhoekstra
 */
public class TaskDescriptionDaoImplTest {
    private static final String TEST_STORAGE_PATH = "test/";

    private final TaskDescriptionDaoImpl taskDescriptionDao;

    public TaskDescriptionDaoImplTest() throws DaoException {
        File file = new File(TEST_STORAGE_PATH);
        file.mkdir();

        taskDescriptionDao = new TaskDescriptionDaoImpl(TEST_STORAGE_PATH);
    }

    @BeforeClass
    public static void setUpClass() {
        File file = new File(TEST_STORAGE_PATH);
        file.mkdir();
    }

    @AfterClass
    public static void tearDownClass() {
        File file = new File(TEST_STORAGE_PATH);
        file.delete();
    }

    @After
    public void tearDown() {
        File file = new File(TEST_STORAGE_PATH);
        for (File taskDescriptionFile : file.listFiles()) {
            taskDescriptionFile.delete();
        }
    }

    @Test
    public void testPersistWithANewObject() {
        TaskDescription taskDescription = new TaskDescription();

        taskDescription.setName("test name");
        taskDescription.setJob("test job");
        taskDescription.setTarget("test target");
        taskDescription.setOutput("test output");
        taskDescription.setStartTime(new Date());
        taskDescription.setEndTime(new Date());
        taskDescription.setStatus(TaskDescription.TaskStatus.PENDING);

        taskDescriptionDao.persist(taskDescription);

        TaskDescription actual = taskDescriptionDao.getById(1);

        // We didn't write a nice mapper for this object so Check if all fields
        // got presisted nicely.
        assertEquals(taskDescription.getName(), actual.getName());
        assertEquals(taskDescription.getJob(), actual.getJob());
        assertEquals(taskDescription.getTarget(), actual.getTarget());
        assertEquals(taskDescription.getOutput(), actual.getOutput());
        assertEquals(taskDescription.getStartTime(), actual.getStartTime());
        assertEquals(taskDescription.getEndTime(), actual.getEndTime());
        assertEquals(taskDescription.getStatus(), actual.getStatus());

        // An id should have been set as well
        assertEquals(new Integer(1), actual.getId());
    }

    @Test
    public void testPersistWithANewObjectShouldSetTheIdOnTheLocalObject() {
        TaskDescription taskDescription = new TaskDescription();
        taskDescriptionDao.persist(taskDescription);

        Integer expected = 1;
        Integer actual = taskDescription.getId();

        assertEquals(expected, actual);
    }

    @Test
    public void testPersistForUpdateingAnObject() {
        // First lets store an object
        TaskDescription taskDescription = new TaskDescription();
        taskDescription.setName("test name");
        taskDescriptionDao.persist(taskDescription);

        taskDescription.setName("New Name");
        taskDescriptionDao.persist(taskDescription);

        // Fetch all the task descriptions
        List<TaskDescription> all = taskDescriptionDao.getAll();

        assertTrue(all.size() == 1);
        assertEquals(new Integer(1), all.get(0).getId());
        assertEquals("New Name", all.get(0).getName());
    }

    @Test
    public void testGetAllReturnsMultipleTaskDescriptions() {
        TaskDescription taskDescription;

        taskDescription = new TaskDescription();
        taskDescription.setName("test 1");
        taskDescriptionDao.persist(taskDescription);

        taskDescription = new TaskDescription();
        taskDescription.setName("test 2");
        taskDescriptionDao.persist(taskDescription);

        // Fetch all the task descriptions
        List<TaskDescription> all = taskDescriptionDao.getAll();

        assertTrue(all.size() == 2);
    }
}
