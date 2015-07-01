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
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;

/**
 *
 * @author mhoekstra
 */
public class DataEnrichmentTask extends EnrichmentTask {


    
    Backend backend;

    @Override
    public TaskResult execute(MetaData metadata) throws TaskException {
        try {
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

            return TaskResult.SUCCESS;
        } catch (BackendException ex) {
            Logger.getLogger(DataEnrichmentTask.class.getName()).log(Level.WARNING, null, ex);
            return TaskResult.FAILURE;
        }
    }
}
