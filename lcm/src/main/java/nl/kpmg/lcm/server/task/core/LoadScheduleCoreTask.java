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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.server.task.CoreTask;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskManager;
import nl.kpmg.lcm.server.task.TaskResult;
import nl.kpmg.lcm.server.task.TaskScheduleException;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.matchers.GroupMatcher;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

/**
 *
 * @author mhoekstra
 */
public class LoadScheduleCoreTask extends CoreTask {

    private static final String GROUP_KEY = "scheduled";

    // @Autowired
    private TaskScheduleDao taskScheduleDao;

    private TaskSchedule current;

    public LoadScheduleCoreTask() {
        taskScheduleDao = Resources.getTaskScheduleDao();
    }

    @Override
    public TaskResult execute() throws TaskException {
        TaskSchedule latest = taskScheduleDao.getCurrent();
        if (current == null || !current.equals(latest)) {
            try {
                removeTasks(scheduler);
            } catch (SchedulerException ex) {
                Logger.getLogger(LoadScheduleCoreTask.class.getName()).log(Level.SEVERE, "couldn't remove the previous schedule.", ex);
                return TaskResult.FAILURE;
            }

            if (latest != null && latest.getItems() != null) {
                for (TaskSchedule.TaskScheduleItem taskScheduleItem : latest.getItems()) {
                    try {
                        schedule(scheduler, taskScheduleItem.getName(), taskScheduleItem.getJob(), taskScheduleItem.getTarget(), taskScheduleItem.getCron());
                    } catch (TaskScheduleException ex) {
                        Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE, "Failed to schedule ", ex);
                    }
                }
            }

            current = latest;
        }
        return TaskResult.SUCCESS;
    }

    private void schedule(Scheduler scheduler, String name, String job, String target, String cron) throws TaskScheduleException {
        try {
            Class<?> cls = Class.forName(job);
            if (EnrichmentTask.class.isAssignableFrom(cls)) {
                schedule(scheduler, name, (Class<EnrichmentTask>) cls, target, cron);
            } else {
                throw new TaskScheduleException("Task definition doesn't contain a schedulable job");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TaskSchedule.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskScheduleException(ex);
        }
    }

    private void schedule(Scheduler scheduler, String name, Class<? extends EnrichmentTask> aClass, String target, String cron) throws TaskScheduleException {
        try {
            JobDetail job = newJob(aClass)
                    .withIdentity(name, GROUP_KEY)
                    .build();

            job.getJobDataMap().put(EnrichmentTask.TARGET, target);

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

    private void removeTasks(Scheduler scheduler) throws SchedulerException {
        Set<JobKey> jobKeys = scheduler.getJobKeys((GroupMatcher<JobKey>) groupEquals(GROUP_KEY));

        for (JobKey jobKey : jobKeys) {
            try {
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException ex) {
                Logger.getLogger(LoadScheduleCoreTask.class.getName()).log(Level.SEVERE, "failed removing task", ex);
            }
        }
    }
}
