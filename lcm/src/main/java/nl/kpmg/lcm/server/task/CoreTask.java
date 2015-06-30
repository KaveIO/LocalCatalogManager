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
package nl.kpmg.lcm.server.task;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.metadata.MetaData;
import nl.kpmg.lcm.server.metadata.storage.MetaDataDao;
import org.apache.commons.lang.NotImplementedException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author mhoekstra
 */
public abstract class CoreTask implements Job {

    public abstract TaskResult execute() throws TaskException;

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Logger.getLogger(CoreTask.class.getName()).entering(this.getClass().getName(), "execute");
            execute();
            Logger.getLogger(CoreTask.class.getName()).exiting(this.getClass().getName(), "execute");
        } catch (TaskException ex) {
            Logger.getLogger(CoreTask.class.getName()).log(Level.SEVERE, "Failed executing task", ex);
        }
    }
}
