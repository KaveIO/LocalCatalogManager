/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.data.dao.mongo;

import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.server.LcmBaseTest;
import nl.kpmg.lcm.common.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;

public class MongoTaskScheduleDaoTest extends LcmBaseTest {

  @Autowired
  TaskScheduleDao taskScheduleDao;

  @Test
  public void testFindLast() throws UserPasswordHashException {
    String firstId = createTaskSchedule();
    TaskSchedule first = taskScheduleDao.findFirstByOrderByIdDesc();
    assertEquals(firstId, first.getId());

    String secondId = createTaskSchedule();
    TaskSchedule second = taskScheduleDao.findFirstByOrderByIdDesc();
    assertEquals(secondId, second.getId());
  }

  private String createTaskSchedule() {
    TaskSchedule taskSchedule = new TaskSchedule();
    LinkedList items = new LinkedList();
    TaskSchedule.TaskScheduleItem taskScheduleItem = new TaskSchedule.TaskScheduleItem();
    items.add(taskScheduleItem);
    taskSchedule.setEnrichmentItems(items);

    TaskSchedule saved = taskScheduleDao.save(taskSchedule);
    return saved.getId();
  }
}
