/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.cron;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import nl.kpmg.lcm.server.cron.exception.CronJobScheduleException;
import nl.kpmg.lcm.server.cron.exception.CronManagerException;
import nl.kpmg.lcm.server.cron.job.AbstractJobScheduler;
import nl.kpmg.lcm.server.cron.job.MainScheduler;
import nl.kpmg.lcm.server.cron.job.processor.DataProcessorScheduler;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * The Singleton class the manages the execution of Tasks. The CronManager uses quartz as a
 scheduling mechanism. All actual work is delegated to quartz jobs or actually their children.
 Currently this uses the singleton design pattern. It is likely this
 should be rewritten as a proper JavaBean.
 *
 * @author mhoekstra
 */
public final class CronManager {

  private final Logger LOGGER = LoggerFactory.getLogger(CronManager.class.getName());
  /***
   * Bellow schedule expressions are valid cron expressions which means that each column
   * representing one metric of the time i.e the first is seconds, the second is minutes, the third
   * is hour etc. "0 * * * * ?"; means that it will execute on the first(0) second of every minute,
   * on every hour every day of month...
   */
  private static final String DATA_PROCESSOR_CRON_SCHEDULE = "0 * * * * ?";
  /*How often task schedule collection is processed(only if it is changed) */
  private static final String TASK_MANAGER_EXECUTION_CRON_SCHEDULE = "0 0 * * * ?";
  /**
   * The group key which is used to register the task which drive the core of the CronManager logic.
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
   * Initializes the scheduler. This method will instantiate and start the scheduler. Additionally
 it will already schedule all core tasks. The core tasks are important to keep the entire system
 up to date and running. Currently two core tasks are scheduled. DataProcessorScheduler Which will
 find tasks with a PENDING state in the task list and execute them. MainScheduler Which
 will update the TaskSchedule of the quartz scheduler if this is changed.
   *
   * @throws CronManagerException if there is a failure in the scheduler initialization
   */
  public void initialize(ApplicationContext context) throws CronManagerException {
    try {
      if (scheduler == null || !scheduler.isStarted()) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(context);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.afterPropertiesSet();

        scheduler = schedulerFactory.getScheduler();

        scheduleJobScheduler("DataProcessorScheduler", DataProcessorScheduler.class,
            DATA_PROCESSOR_CRON_SCHEDULE);
        scheduleJobScheduler("mainScheduler", MainScheduler.class,
            TASK_MANAGER_EXECUTION_CRON_SCHEDULE);

        scheduler.start();
      } else {
        throw new CronManagerException(
            "Trying to initialize the TaskManager while its already running.");
      }
    } catch (SchedulerException ex) {
      LOGGER.error("Initialization of the quartz scheduler failed", ex);

      throw new CronManagerException(ex);
    } catch (CronJobScheduleException ex) {
      LOGGER.error("Core task couldn't be scheduled", ex);

      throw new CronManagerException(ex);
    } catch (Exception ex) {
      LOGGER.error("Initialization of the quartz scheduler failed", ex);

      throw new CronManagerException(ex);
    }
  }

  /**
   * Gets the scheduler. You probably don't need this! This is currently needed by
 MainScheduler but in most other cases direct access to the scheduler is at least
 suspicious.
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
   * @throws CronJobScheduleException if the job can't be scheduled
   */
  private void scheduleJobScheduler(final String name, final Class<? extends AbstractJobScheduler> aClass,
      final String cron) throws CronJobScheduleException {
    try {
      JobDetail job = newJob(aClass).withIdentity(name, GROUP_KEY).build();

      job.getJobDataMap().put(AbstractJobScheduler.SCHEDULER, scheduler);

      CronTrigger trigger =
          newTrigger().withIdentity(name, GROUP_KEY).withSchedule(cronSchedule(cron)).build();

      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException ex) {
      LOGGER.error(null, ex);
      throw new CronJobScheduleException(ex);
    }
  }

  /**
   * Stop the scheduler.
   */
  public void stop() {
    try {
      scheduler.shutdown();
    } catch (SchedulerException ex) {
      LOGGER.warn("failed shuting down the schedulere", ex);
    }
  }
}
