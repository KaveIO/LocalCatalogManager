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

import nl.kpmg.lcm.common.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoTaskScheduleDao
    extends MongoRepository<TaskSchedule, String>, TaskScheduleDao {

}
