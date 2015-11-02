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
package nl.kpmg.lcm.server.task.enrichment;

import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendException;
import nl.kpmg.lcm.server.backend.DataSetInformation;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Fills data on a MetaData object concerning the described Data.
 *
 * MetaData describes data. This Task will check through the appropriate backend
 * if the data in question is actually attached and/or readable from the LCM.
 * This will update and overwrite the specific piece of MetaData. The data
 * added contains:
 *   - state                : DETACHED | ATTACHED
 *   - readable             : UNREADABLE | READABLE
 *   - size                 : byte-size
 *   - update-timestamp     : date
 *
 * this will be set on the following path:
 *
 * {
 *   "dynamic": {
 *     "data" {
 *       "state"
 *       "readable"
 *       "size"
 *       "update-timestamp"
 *     }
 *   }
 * }
 *
 * @author mhoekstra
 */
public class DataEnrichmentTask extends EnrichmentTask {

    /**
     * The MetaDataDao.
     */
    @Autowired
    private MetaDataDao metaDataDao;

    /**
     * The BackendService.
     */
    @Autowired
    private StorageService storageService;

    /**
     * Will store information on the data associated with a piece of MetaData.
     *
     * @param metadata the metadata to enrich
     * @return the result of the task
     * @throws TaskException if the backend fails
     */
    @Override
    protected final TaskResult execute(final MetaData metadata) throws TaskException {
        try {
            Backend backend = storageService.getBackend(metadata);
            if (backend == null) {
                return TaskResult.FAILURE;
            }

            DataSetInformation gatherDataSetInformation = backend.gatherDataSetInformation(metadata);

            if (!gatherDataSetInformation.isAttached()) {
                metadata.set("dynamic.data.state", "DETACHED");
                return TaskResult.SUCCESS;
            }
            metadata.set("dynamic.data.state", "ATTACHED");

            if (!gatherDataSetInformation.isReadable()) {
                metadata.set("dynamic.data.readable", "UNREADABLE");
                return TaskResult.SUCCESS;
            }
            metadata.set("dynamic.data.readable", "READABLE");
            metadata.set("dynamic.data.size", gatherDataSetInformation.getByteSize());
            metadata.set("dynamic.data.update-timestamp", gatherDataSetInformation.getModificationTime().toString());

            metaDataDao.persist(metadata);

            return TaskResult.SUCCESS;
        } catch (BackendException ex) {
            Logger.getLogger(DataEnrichmentTask.class.getName()).log(Level.WARNING, null, ex);
            return TaskResult.FAILURE;
        }
    }
}
