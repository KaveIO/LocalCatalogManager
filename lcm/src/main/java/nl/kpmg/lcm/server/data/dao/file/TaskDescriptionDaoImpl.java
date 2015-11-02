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
package nl.kpmg.lcm.server.data.dao.file;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;

/**
 * Implementation of a file based Task DAO.
 */
public class TaskDescriptionDaoImpl extends AbstractGenericFileDaoImpl<TaskDescription> implements TaskDescriptionDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(TaskDescriptionDaoImpl.class.getName());

    /**
     * @param storagePath The path where the task is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public TaskDescriptionDaoImpl(final String storagePath) throws DaoException {
        super(storagePath, TaskDescription.class);
    }

    /**
     * Get List of TaskDescription objects By Task Status.
     *
     * @see
     * nl.kpmg.lcm.server.data.dao.TaskDescriptionDao#getByStatus(nl.kpmg.lcm.server.data.TaskDescription.TaskStatus)
     */
    @Override
    public List<TaskDescription> getByStatus(TaskDescription.TaskStatus status) {
        ArrayList<TaskDescription> result = new ArrayList<TaskDescription>();
        List<TaskDescription> allTasks = getAll();
        for (TaskDescription task : allTasks) {
            if (task.getStatus() == status) {
                result.add(task);
            }
        }
        return result;
    }

    /**
     * Update the original TaskDescription with updated one
     *
     * @see
     * nl.kpmg.lcm.server.data.dao.file.AbstractGenericFileDaoImpl#update(nl.kpmg.lcm.server.data.AbstractModel,
     * nl.kpmg.lcm.server.data.AbstractModel)
     */
    @Override
    protected void update(TaskDescription original, TaskDescription update) {
        original.setJob(update.getJob());
        original.setStatus(update.getStatus());
        original.setOutput(update.getOutput());
        original.setTarget(update.getTarget());
        original.setStartTime(update.getStartTime());
        original.setEndTime(update.getEndTime());
    }

    @Override
    public boolean isValid(TaskDescription object) {
        return true;
    }
}
