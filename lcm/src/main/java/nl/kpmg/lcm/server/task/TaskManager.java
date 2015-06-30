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

import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.task.core.LoadScheduleCoreTask;
import nl.kpmg.lcm.server.task.core.ExecuteTasksCoreTask;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author mhoekstra
 */
public class TaskManager {

    private static final String GROUP_KEY = "core";

    private static TaskManager instance;

    private Scheduler scheduler;

    private TaskManager() { }

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public boolean isInitialized() {
        try {
            return scheduler != null && scheduler.isStarted();
        } catch (SchedulerException ex) {
            return false;
        }
    }

    public void initialize() throws TaskManagerException {
        try {
            if (scheduler == null || !scheduler.isStarted()) {
                SchedulerFactory sf = new StdSchedulerFactory();
                scheduler = sf.getScheduler();

                schedule("executeTasksCoreTask", ExecuteTasksCoreTask.class, "0 * * * * ?");
                schedule("loadScheduleCoreTask", LoadScheduleCoreTask.class, "0 * * * * ?");

                scheduler.start();
            } else {
                throw new TaskManagerException("Trying to initialize the TaskManager while its already running.");
            }
        } catch (SchedulerException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE, "Initialization of the quartz scheduler failed", ex);
            throw new TaskManagerException(ex);
        } catch (TaskScheduleException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE, "Core task couldn't be scheduled", ex);
            throw new TaskManagerException(ex);
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    private void schedule(String name, Class<? extends CoreTask> aClass, String cron) throws TaskScheduleException {
        try {
            JobDetail job = newJob(aClass)
                    .withIdentity(name, GROUP_KEY)
                    .build();

            CronTrigger trigger = newTrigger()
                    .withIdentity(name, GROUP_KEY)
                    .withSchedule(cronSchedule(cron))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskScheduleException(ex);
        }
    }
}
