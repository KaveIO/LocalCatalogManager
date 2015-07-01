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
package nl.kpmg.lcm.server.task.core;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;
import nl.kpmg.lcm.server.task.CoreTask;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;
import org.apache.commons.lang.NotImplementedException;

/**
 *
 * @author mhoekstra
 */
public class ExecuteTasksCoreTask extends CoreTask {

    private MetaDataDao metaDataDao;
    private TaskDescriptionDao taskDescriptionDao;

    public ExecuteTasksCoreTask() {
        metaDataDao = Resources.getMetaDataDao();
        taskDescriptionDao = Resources.getTaskDescriptionDao();
    }

    @Override
    public TaskResult execute() throws TaskException {
        List<TaskDescription> taskDescriptions = taskDescriptionDao.getAll();
        for (TaskDescription taskDescription : taskDescriptions) {
            if (taskDescription.getStatus() == TaskDescription.TaskStatus.PENDING) {
                taskDescription.setStatus(TaskDescription.TaskStatus.RUNNING);
                taskDescriptionDao.persist(taskDescription);


                TaskResult result = execute(taskDescription);


                return result;
            }
        }
        return TaskResult.SUCCESS;
    }

    private TaskResult execute(TaskDescription taskDescription) throws TaskException {
        TaskResult result = TaskResult.SUCCESS;
        String target = taskDescription.getTarget();

        List<MetaData> targets;
        try {
            targets = fetchTargets(target);
        } catch (TaskException ex) {
            Logger.getLogger(EnrichmentTask.class.getName()).log(Level.SEVERE, "Couldn't fetch targets for Job.", ex);
            throw new TaskException(ex);
        }

        EnrichmentTask enrichmentTask = getEnrichmentTaskInstance(taskDescription.getJob());
        if (targets != null) {
            for (MetaData metadata : targets) {
                try {
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.INFO, "Executing task for metadata");
                    result = enrichmentTask.execute(metadata);
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.INFO, "Done with task");
                } catch (TaskException ex) {
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.SEVERE, "Failed executing task", ex);
                    result = TaskResult.FAILURE;
                }
            }
        } else {
            return TaskResult.FAILURE;
        }

        return result;
    }

    private List<MetaData> fetchTargets(String expression) throws TaskException {
        /** @TODO this should go in a service layer. This has no place here. */
        List<MetaData> targets = new LinkedList();

        if (expression.length() == 0) {
            throw new TaskException("Target expression is empty");
        }

        String[] split = expression.split("/");
        if (split.length == 1) {
            if (split[0].equals("*")) {
                targets = metaDataDao.getAll();
            } else {
                targets.add(metaDataDao.getByName(split[0]));
            }
        } else if (split.length == 2) {
            if (split[0].equals("*")) {
                throw new NotImplementedException("Scheduling on */* is not implemented yet.");
            } else {
                if (split[1].equals("*")) {
                    throw new NotImplementedException("Scheduling on ???/* is not implemented yet.");
                } else {
                    targets.add(metaDataDao.getByNameAndVersion(split[0], split[1]));
                }
            }
        } else {
            throw new TaskException("Target expression has an unknown format");
        }
        return targets;
    }

    private EnrichmentTask getEnrichmentTaskInstance(String job) throws TaskException {
        try {
            Class<?> cls = Class.forName(job);
            if (EnrichmentTask.class.isAssignableFrom(cls)) {
                return (EnrichmentTask) cls.newInstance();
            } else {
                throw new TaskException("Task definition doesn't contain a schedulable job");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TaskSchedule.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskException(ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ExecuteTasksCoreTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskException(ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ExecuteTasksCoreTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskException(ex);
        }
    }
}
