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
package nl.kpmg.lcm.server.data.service;

import java.util.LinkedList;
import java.util.List;
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import org.apache.commons.lang.NotImplementedException;

/**
 *
 * @author mhoekstra
 */
public class MetaDataService {

    private final MetaDataDao metaDataDao;

    public MetaDataService() {
        this.metaDataDao = Resources.getMetaDataDao();
    }

    public List<MetaData> getByExpression(String expression) throws ServiceException {
        List<MetaData> targets = new LinkedList();

        if (expression.length() == 0) {
            throw new ServiceException("Target expression is empty");
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
            throw new ServiceException("Target expression has an unknown format");
        }
        return targets;
    }
}
