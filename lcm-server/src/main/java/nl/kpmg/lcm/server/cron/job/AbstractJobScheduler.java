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

package nl.kpmg.lcm.server.cron.job;

import nl.kpmg.lcm.server.cron.TaskResult;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A core task is a task which is needed to keep the system itself running.
 *
 * Core tasks aren't run on specific MetaData items but can retrieve them if necessary.
 *
 * @author mhoekstra
 */
public abstract class AbstractJobScheduler implements Job {
  /**
   * The field name containing the scheduler in the JobDataMap.
   */
  public static final String SCHEDULER = "scheduler";

  private final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class.getName());
  /**
   * The quartz scheduler.
   */
  private Scheduler scheduler;

  /**
   * Method called to process the actual code of this task.
   *
   * @return The result of the task
   * @throws CronJobExecutionException if the task can't be executed properly
   */
  protected abstract TaskResult execute() throws CronJobExecutionException;

  /**
   * Execute method invoked by the quartz scheduler.
   *
   * @param context provided by quartz
   * @throws JobExecutionException if the job couldn't be executed properly.
   */
  @Override
  public final void execute(final JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    scheduler = (Scheduler) jobDataMap.get(SCHEDULER);

    try {
      LOGGER.trace(
          String.format("Executing JobScheduler %s ", context.getJobDetail().getKey().getName()));

      execute();

      LOGGER.trace(
          String.format("Done with JobScheduler %s ", context.getJobDetail().getKey().getName()));
    } catch (Exception ex) {
      LOGGER.error( "Failed executing task", ex);
      throw new JobExecutionException(ex);
    }
  }

  /**
   * @return the quartz scheduler
   */
  protected final Scheduler getScheduler() {
    return scheduler;
  }
}
