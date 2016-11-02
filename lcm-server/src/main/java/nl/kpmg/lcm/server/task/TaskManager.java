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
import static org.quartz.TriggerBuilder.newTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * The Singleton class the manages the execution of Tasks.
 *
 * The TaskManager uses quartz as a scheduling mechanism. All actual work is
 * delegated to quartz jobs or actually their children: CoreTask and
 * EnrichmentTask.
 *
 * Currently this uses the singleton design pattern. It is likely this should
 * be rewritten as a proper JavaBean.
 *
 * @author mhoekstra
 */
public final class TaskManager {

    /***
     * Bellow schedule expressions are valid cron expressions which means that 
     * each column representing one metric of the time i.e
     * the first is seconds, the second is minutes, the third is hour  etc.
     * "0 * * * * ?"; means that it will execute on 
     * the first(0) second of every minute, on every hour ievery day of month...
     */
   private static final String EXECUTE_CORE_TASK_CRON_SCHEDULE = "0 * * * * ?";
   private static final String LOAD_CORE_TASK_CRON_SCHEDULE = "0 * * * * ?";
    /**
     * The group key which is used to register the task which drive the core
     * of the TaskManager logic.
     */
    private static final String GROUP_KEY = "core";

    /**
     * The quartz scheduler that actually takes care of the execution of tasks.
     */
    private Scheduler scheduler;

    /**
     * @return true if the scheduler is initialized.
     */
    public boolean isInitialized() {
        try {
            return scheduler != null && scheduler.isStarted();
        } catch (SchedulerException ex) {
            return false;
        }
    }

    /**
     * Initializes the scheduler.
     *
     * This method will instantiate and start the scheduler. Additionally it will
     * already schedule all core tasks. The core tasks are important to keep the
     * entire system up to date and running. Currently two core tasks are scheduled.
     *
     * ExecuteTasksCoreTask
     *    Which will find tasks with a PENDING state in the task list and execute
     *    them.
     *
     * LoadScheduleCoreTask
     *    Which will update the TaskSchedule of the quartz scheduler if this is
     *    changed.
     *
     * @throws TaskManagerException if there is a failure in the scheduler initialization
     */
    public void initialize(ApplicationContext context) throws TaskManagerException {
        try {
            if (scheduler == null || !scheduler.isStarted()) {
                SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
                AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
                jobFactory.setApplicationContext(context);
                schedulerFactory.setJobFactory(jobFactory);
                schedulerFactory.afterPropertiesSet();

                scheduler = schedulerFactory.getScheduler();

                scheduleCoreTask("executeTasksCoreTask", ExecuteTasksCoreTask.class, EXECUTE_CORE_TASK_CRON_SCHEDULE);
                scheduleCoreTask("loadScheduleCoreTask", LoadScheduleCoreTask.class, LOAD_CORE_TASK_CRON_SCHEDULE);

                scheduler.start();
            } else {
                throw new TaskManagerException("Trying to initialize the TaskManager while its already running.");
            }
        } catch (SchedulerException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE,
                    "Initialization of the quartz scheduler failed", ex);

            throw new TaskManagerException(ex);
        } catch (TaskScheduleException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE,
                    "Core task couldn't be scheduled", ex);

            throw new TaskManagerException(ex);
        } catch (Exception ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.SEVERE,
                    "Initialization of the quartz scheduler failed", ex);

            throw new TaskManagerException(ex);
        }
    }

    /**
     * Gets the scheduler.
     *
     * You probably don't need this! This is currently needed by LoadScheduleCoreTask
     * but in most other cases direct access to the scheduler is at least suspicious.
     *
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Method for simplifying the scheduling of core tasks.
     *
     * @param name The name of the task
     * @param aClass The class which will be executed
     * @param cron The cron definition used for triggering this task
     * @throws TaskScheduleException if the job can't be scheduled
     */
    private void scheduleCoreTask(final String name, final Class<? extends CoreTask> aClass,
            final String cron) throws TaskScheduleException {
        try {
            JobDetail job = newJob(aClass)
                    .withIdentity(name, GROUP_KEY)
                    .build();

            job.getJobDataMap().put(CoreTask.SCHEDULER, scheduler);

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

    /**
     * Stop the scheduler.
     */
    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            Logger.getLogger(TaskManager.class.getName()).log(Level.WARNING, "failed shuting down the schedulere", ex);
        }
    }
}
