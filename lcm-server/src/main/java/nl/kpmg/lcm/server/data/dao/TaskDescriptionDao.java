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

package nl.kpmg.lcm.server.data.dao;

import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 *
 * @author mhoekstra
 */
public interface TaskDescriptionDao extends PagingAndSortingRepository<TaskDescription, String> {
  public List<TaskDescription> findByStatus(TaskDescription.TaskStatus status);

  public List<TaskDescription> findByStatus(TaskDescription.TaskStatus status, Sort sort);

  public List<TaskDescription> findByStatus(TaskDescription.TaskStatus status, Pageable pageable);

  public List<TaskDescription> findByType(TaskType type);

  public List<TaskDescription> findByType(TaskType type, Pageable pageable);

  public List<TaskDescription> findByType(TaskType type, Sort sort);

  public List<TaskDescription> findByTypeAndStatus(TaskType type, TaskDescription.TaskStatus status);

  public List<TaskDescription> findByTypeAndStatus(TaskType type,
      TaskDescription.TaskStatus status, Pageable pageable);

  public List<TaskDescription> findByTypeAndStatus(TaskType type,
      TaskDescription.TaskStatus status, Sort sort);
}
