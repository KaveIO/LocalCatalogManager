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
public abstract class EnrichmentTask implements Job {

    public static final String TARGET = "target";


    MetaDataDao metaDataDao;

    public abstract TaskResult execute(MetaData metadata) throws TaskException;


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

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String target = jobDataMap.getString(TARGET);

        List<MetaData> targets;
        try {
            targets = fetchTargets(target);
        } catch (TaskException ex) {
            Logger.getLogger(EnrichmentTask.class.getName()).log(Level.SEVERE, "Couldn't fetch targets for Job.", ex);
            throw new JobExecutionException(ex);
        }

        if (targets != null) {
            for (MetaData metadata : targets) {
                try {
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.INFO, "Executing task for metadata");
                    execute(metadata);
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.INFO, "Done with task");
                } catch (TaskException ex) {
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.SEVERE, "Failed executing task", ex);
                }
            }
        }
    }
}
