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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of a file based TaskSchedule DAO.
 */
public class TaskScheduleDaoImpl extends AbstractGenericFileDaoImpl<TaskSchedule> implements TaskScheduleDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(TaskScheduleDaoImpl.class.getName());

    

    /**
     * @param storagePath The path where the taskSchedule is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public TaskScheduleDaoImpl(final String storagePath) throws DaoException {
        super(storagePath,TaskSchedule.class);    	
    }       
  
    /** 
     * Get the current TaskSchedule from storage
     * @see nl.kpmg.lcm.server.data.dao.TaskScheduleDao#getCurrent()
     */
    @Override
    public TaskSchedule getCurrent() {
        File taskScheduleFolder = storage;
        return getById(taskScheduleFolder.list().length);
    }
     
	/**
	 * Update original TaskSchedule with updated TaskSchedule 
	 * @see nl.kpmg.lcm.server.data.dao.file.AbstractGenericFileDaoImpl#update(nl.kpmg.lcm.server.data.AbstractModel, nl.kpmg.lcm.server.data.AbstractModel)
	 */
	@Override
	protected void update(TaskSchedule original, TaskSchedule update) {
		original.setName(update.getName());
		original.setItems(update.getItems());		
	}
}
