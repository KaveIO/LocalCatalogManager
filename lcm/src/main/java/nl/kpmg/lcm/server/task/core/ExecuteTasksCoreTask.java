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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;
import nl.kpmg.lcm.server.task.CoreTask;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskManager;
import nl.kpmg.lcm.server.task.TaskResult;
import nl.kpmg.lcm.server.task.TaskScheduleException;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Executor of ad-hoc tasks.
 *
 * EnrichmentTasks can be created either through a schedule or in an ad-hoc way.
 * These ad-hoc tasks will be scheduled by this Task.
 *
 * @author mhoekstra
 */
public class ExecuteTasksCoreTask extends CoreTask {

    /**
     * The group key which is used to register the ad-hoc tasks.
     */
    private static final String GROUP_KEY = "adhoc";

    /**
     * The TaskDescriptionDao.
     * @TODO Auto wire this.
     */
    private final TaskDescriptionDao taskDescriptionDao;

    /**
     * Default constructor.
     *
     * Only needed because we can't autowire the dao's yet.
     */
    public ExecuteTasksCoreTask() {
        taskDescriptionDao = Resources.getTaskDescriptionDao();
    }

    /**
     * Schedules all tasks with the status PENDING for direct execution.
     *
     * @return the result of the scheduling
     * @throws TaskException if the task fails to execute
     */
    @Override
    public final TaskResult execute() throws TaskException {

        List<TaskDescription> taskDescriptions = taskDescriptionDao.getAll();
        for (TaskDescription taskDescription : taskDescriptions) {
            if (taskDescription.getStatus() == TaskDescription.TaskStatus.PENDING) {
                try {
                    scheduleEnrichmentTask(
                            taskDescription.getName(),
                            taskDescription.getJob(),
                            taskDescription.getTarget());
                    taskDescription.setStatus(TaskDescription.TaskStatus.SCHEDULED);
                    taskDescriptionDao.persist(taskDescription);
                } catch (TaskScheduleException ex) {
                    Logger.getLogger(ExecuteTasksCoreTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return TaskResult.SUCCESS;
    }

    /**
     * Schedules an EnrichmentTask for immediate execution.
     *
     * @param name The name of the job
     * @param job the class name of the job to execute
     * @param target the target MetaData expression
     * @throws TaskScheduleException when the task couldn't be scheduled
     */
    private void scheduleEnrichmentTask(final String name, final String job, final String target)
            throws TaskScheduleException {
        try {
            Class<? extends EnrichmentTask> enrichmentTaskClass = getEnrichmentTaskClass(job);
            scheduleEnrichmentTask(name, enrichmentTaskClass, target);
        } catch (TaskException ex) {
            Logger.getLogger(ExecuteTasksCoreTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskScheduleException(ex);
        }
    }

    /**
     * Schedules an EnrichmentTask for immediate execution.
     *
     * @param name The name of the job
     * @param job the class of the job to execute
     * @param target the target MetaData expression
     * @throws TaskScheduleException when the task couldn't be scheduled
     */
    private void scheduleEnrichmentTask(final String name, final Class<? extends EnrichmentTask> job,
            final String target) throws TaskScheduleException {
        try {
            JobDetail jobDetail = newJob(job)
                    .withIdentity(name, GROUP_KEY)
                    .build();
            jobDetail.getJobDataMap().put(EnrichmentTask.TARGET_KEY, target);

            Scheduler scheduler = getScheduler();
            scheduler.addJob(jobDetail, true);
            scheduler.triggerJob(jobDetail.getKey());
        } catch (SchedulerException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskScheduleException(ex);
        }
    }

    /**
     * Method for translating a class name to a specific Class instance.
     *
     * This will check if the provided class name actually extends an EnrichmentTask
     *
     * @param className the name of the class
     * @return the class instance of the class
     * @throws TaskException when className is not correct or points to something else than an EnrichmentTask
     */
    private Class<? extends EnrichmentTask> getEnrichmentTaskClass(final String className) throws TaskException {
        try {
            Class<?> cls = Class.forName(className);
            if (EnrichmentTask.class.isAssignableFrom(cls)) {
                return (Class<? extends EnrichmentTask>) cls;
            } else {
                throw new TaskException("Task definition doesn't contain a schedulable job");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TaskSchedule.class.getName()).log(Level.SEVERE, null, ex);
            throw new TaskException(ex);
        }
    }
}
