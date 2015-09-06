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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;
import nl.kpmg.lcm.server.data.TaskDescription;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of a file based Task DAO.
 */
public class TaskDescriptionDaoImpl implements TaskDescriptionDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(TaskDescriptionDaoImpl.class.getName());

    /**
     * Path where the task is stored.
     */
    private final File storage;

    /**
     * Object mapper used to serialize and de-serialize the task.
     */
    private final ObjectMapper mapper;

    /**
     * @param storagePath The path where the task is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    @Autowired
    public TaskDescriptionDaoImpl(final String storagePath, final ObjectMapper mapper) throws DaoException {
        this.storage = new File(storagePath);
        this.mapper = mapper;

        if (!storage.isDirectory() || !this.storage.canWrite()) {
            throw new DaoException(String.format(
                    "The storage path %s is not a directory or not writable.", storage.getAbsolutePath()));
        }
    }

    private File getTaskFolder() {
        return storage;
    }

    private File getTaskFile(Integer id) {
        return new File(String.format("%s/%s", storage, id));
    }

    @Override
    public List<TaskDescription> getAll() {
        String[] allTaskDescriptionIds = storage.list();
        LinkedList<TaskDescription> result = new LinkedList();

        for (String taskDescriptionId : allTaskDescriptionIds) {
            TaskDescription taskDescription = getById(Integer.parseInt(taskDescriptionId));
            if (taskDescription != null) {
                result.add(taskDescription);
            }
        }
        return result;
    }

    @Override
    public TaskDescription getById(Integer id) {
        try {
            TaskDescription taskDescription = mapper.readValue(getTaskFile(id), TaskDescription.class);
            if (taskDescription != null) {
                taskDescription.setId(id);
            }
            return taskDescription;
        } catch (IOException ex) {
            Logger.getLogger(TaskDescriptionDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<TaskDescription> getByStatus(TaskDescription.TaskStatus status) {
        LinkedList<TaskDescription> result = new LinkedList();
        List<TaskDescription> allTasks = getAll();
        for (TaskDescription task : allTasks) {
            if (task.getStatus() == status) {
                result.add(task);
            }
        }
        return result;
    }

    @Override
    public void persist(TaskDescription taskDescription) {
        Integer id = taskDescription.getId();

        if (id == null) {
            File taskDescriptionFolder = getTaskFolder();
            id = taskDescriptionFolder.list().length + 1;
        }

        try {
            mapper.writeValue(getTaskFile(id), taskDescription);
            taskDescription.setId(id);
        } catch (IOException ex) {
            Logger.getLogger(TaskDescriptionDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void delete(TaskDescription task) {
        File taskDescriptionFile = getTaskFile(task.getId());
        taskDescriptionFile.delete();
    }
}
