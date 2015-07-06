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
import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.server.data.TaskSchedule;

/**
 * Implementation of a file based TaskSchedule DAO.
 */
public class TaskScheduleDaoImpl implements TaskScheduleDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(TaskScheduleDaoImpl.class.getName());

    /**
     * Path where the taskSchedule is stored.
     */
    private final File storage;

    /**
     * Object mapper used to serialize and de-serialize the taskSchedule.
     */
    private final ObjectMapper mapper;

    /**
     * @param storagePath The path where the taskSchedule is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public TaskScheduleDaoImpl(final String storagePath) throws DaoException {
        storage = new File(storagePath);

        JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();
        mapper = jacksonJsonProvider.getContext(TaskSchedule.class);

        if (!storage.isDirectory() || !this.storage.canWrite()) {
            throw new DaoException(String.format(
                    "The storage path %s is not a directory or not writable.", storage.getAbsolutePath()));
        }
    }

    private File getTaskScheduleFolder() {
        return storage;
    }

    private File getTaskScheduleFile(Integer id) {
        return new File(String.format("%s/%s", storage, id));
    }

    @Override
    public TaskSchedule getById(Integer id) {
        try {
            TaskSchedule taskSchedule = mapper.readValue(getTaskScheduleFile(id), TaskSchedule.class);
            if (taskSchedule != null) {
                taskSchedule.setId(id);
            }
            return taskSchedule;
        } catch (IOException ex) {
            Logger.getLogger(TaskScheduleDaoImpl.class.getName()).log(Level.FINER,
                    String.format("Couldn't construct TaskSchedule with id %s", id), ex);
            return null;
        }
    }

    @Override
    public List<TaskSchedule> getAll() {
        String[] allTaskScheduleIds = storage.list();
        LinkedList<TaskSchedule> result = new LinkedList();

        for (String taskScheduleId : allTaskScheduleIds) {
            TaskSchedule taskSchedule = getById(Integer.parseInt(taskScheduleId));
            if (taskSchedule != null) {
                result.add(taskSchedule);
            }
        }
        return result;
    }

    @Override
    public TaskSchedule getCurrent() {
        File taskScheduleFolder = getTaskScheduleFolder();
        return getById(taskScheduleFolder.list().length);
    }

    @Override
    public void persist(TaskSchedule taskSchedule) {
        Integer id = taskSchedule.getId();

        if (id == null) {
            File taskScheduleFolder = getTaskScheduleFolder();
            id = taskScheduleFolder.list().length + 1;
        }

        try {
            mapper.writeValue(getTaskScheduleFile(id), taskSchedule);
            taskSchedule.setId(id);
        } catch (IOException ex) {
            Logger.getLogger(TaskScheduleDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void delete(TaskSchedule taskSchedule) {
        File taskScheduleFile = getTaskScheduleFile(taskSchedule.getId());
        taskScheduleFile.delete();
    }
}
