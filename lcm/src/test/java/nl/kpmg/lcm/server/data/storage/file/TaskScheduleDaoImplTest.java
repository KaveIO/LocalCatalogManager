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

import nl.kpmg.lcm.server.data.dao.file.TaskScheduleDaoImpl;
import java.util.LinkedList;
import java.util.List;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.TaskSchedule.TaskScheduleItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
public class TaskScheduleDaoImplTest extends LCMBaseTest {

    @Autowired
    private TaskScheduleDaoImpl taskScheduleDao;

    @Test
    public void testPersistWithANewObject() {
        TaskSchedule taskSchedule = new TaskSchedule();
        taskSchedule.setItems(new LinkedList());

        List<TaskScheduleItem> taskScheduleItems = taskSchedule.getItems();

        TaskScheduleItem taskScheduleItem;
        taskScheduleItem = new TaskScheduleItem();
        taskScheduleItem.setName("test name");
        taskScheduleItem.setJob("test job");
        taskScheduleItem.setCron("test cron");
        taskScheduleItem.setTarget("test target");

        taskScheduleItems.add(taskScheduleItem);

        taskScheduleDao.persist(taskSchedule);


        TaskSchedule actual = taskScheduleDao.getById(taskSchedule.getId());

        List<TaskScheduleItem> items = actual.getItems();

        assertTrue(items.size() == 1);
        assertEquals(taskScheduleItem.getName(), items.get(0).getName());
        assertEquals(taskScheduleItem.getJob(), items.get(0).getJob());
        assertEquals(taskScheduleItem.getCron(), items.get(0).getCron());
        assertEquals(taskScheduleItem.getTarget(), items.get(0).getTarget());

        // An id should have been set as well
        assertNotNull(actual.getId());
    }

    @Test
    public void testPersistWithANewObjectShouldSetTheIdOnTheLocalObject() {
        TaskSchedule taskSchedule = new TaskSchedule();
        taskScheduleDao.persist(taskSchedule);

        assertNotNull(taskSchedule.getId());
    }

    @Test
    public void testPersistForUpdateingAnObject() {
        // First lets store an object
        TaskSchedule taskSchedule = new TaskSchedule();

        List<TaskScheduleItem> taskScheduleItems = taskSchedule.getItems();

        TaskScheduleItem taskScheduleItem;
        taskScheduleItem = new TaskScheduleItem();
        taskScheduleItem.setName("test name");

        taskScheduleDao.persist(taskSchedule);

        // lets remove all items from the list.
        taskSchedule.getItems().clear();
        taskScheduleDao.persist(taskSchedule);

        // Fetch all the task descriptions
        List<TaskSchedule> all = taskScheduleDao.getAll();

        assertTrue(all.size() == 1);
        assertNotNull(all.get(0).getId());
        assertEquals(0, all.get(0).getItems().size());
    }
}
