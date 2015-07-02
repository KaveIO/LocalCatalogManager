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

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.ServiceException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A task which is being applied on MetaData to enrich its content.
 *
 * @author mhoekstra
 */
public abstract class EnrichmentTask implements Job {

    /**
     * The field name containing the metadata expression in the JobDataMap.
     */
    public static final String TARGET_KEY = "target";

    /**
     * The field name containing the task id in the JobDataMap.
     */
    public static final String TASK_ID_KEY = "task_id";

    /**
     * The MetaDataService.
     *
     * @TODO Auto wire this.
     */
    private final MetaDataService metaDataService;

    /**
     * The TaskDescriptionDao.
     *
     * @TODO Auto wire this.
     */
    private final TaskDescriptionDao taskDescriptionDao;


    /**
     * Default constructor.
     *
     * Only needed because we can't autowire the dao's yet.
     */
    public EnrichmentTask() {
        metaDataService = Resources.getMetaDataService();
        taskDescriptionDao = Resources.getTaskDescriptionDao();
    }

    /**
     * Method called to process the actual code of this task.
     *
     * @param metadata the MetaData to apply this task on
     * @return The result of the task
     * @throws TaskException if the task can't be executed properly
     */
    protected abstract TaskResult execute(MetaData metadata) throws TaskException;

    /**
     * Execute method invoked by the quartz scheduler.
     *
     * Interprets the target in the JobDataMap and invokes execute for each of
     * them. If the task fails for any of the MetaData the execution cycle will
     * keep going.
     *
     * @param context provided by quartz
     * @throws JobExecutionException if the job couldn't be executed properly.
     */
    @Override
    public final void execute(final JobExecutionContext context) throws JobExecutionException {

        // Fetch information from the job context.
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String target = jobDataMap.getString(TARGET_KEY);
        Integer taskId = jobDataMap.getInt(TASK_ID_KEY);

        // Update or create the task description
        TaskDescription taskDescription;
        if (taskId == 0) {
            taskDescription = taskDescriptionDao.getById(taskId);
        } else {
            taskDescription = new TaskDescription();
            taskDescription.setJob(this.getClass().getName());
            taskDescription.setTarget(target);
            taskDescription.setName(context.getJobDetail().getKey().getName());
        }
        taskDescription.setStatus(TaskDescription.TaskStatus.RUNNING);
        taskDescription.setStartTime(new Date());
        taskDescriptionDao.persist(taskDescription);

        // Find the MetaData target on which this job needs to be executed
        List<MetaData> targets;
        try {
            targets = metaDataService.getByExpression(target);
        } catch (ServiceException ex) {
            Logger.getLogger(EnrichmentTask.class.getName()).log(Level.SEVERE, "Couldn't fetch targets for Job.", ex);
            throw new JobExecutionException(ex);
        }

        // Execute the actuall code for each target
        if (targets != null) {
            for (MetaData metadata : targets) {
                try {
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.INFO,
                            String.format("Executing EnrichmentTask %s (%s)",
                                    taskDescription.getName(), taskDescription.getJob()));

                    execute(metadata);

                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.INFO,
                            String.format("Done with EnrichmentTask %s (%s)",
                                    taskDescription.getName(), taskDescription.getJob()));
                } catch (TaskException ex) {
                    Logger.getLogger(EnrichmentTask.class.getName()).log(Level.SEVERE, "Failed executing task", ex);
                }
            }
        }

        taskDescription.setStatus(TaskDescription.TaskStatus.SUCCESS);
        taskDescription.setEndTime(new Date());
        taskDescriptionDao.persist(taskDescription);
    }
}
