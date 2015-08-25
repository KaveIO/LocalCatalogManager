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
package nl.kpmg.lcm.server.data.storage.file;

import nl.kpmg.lcm.server.data.dao.file.MetaDataDaoImpl;
import java.util.List;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.MetaData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
public class MetaDataDaoImplTest extends LCMBaseTest {

    @Autowired
    private MetaDataDaoImpl metaDataDao;

    @Test
    public void testPersist() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);
    }

    @Test
    public void testPersistVersionZero() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);

        MetaData versionZeroExists = metaDataDao.getByNameAndVersion("test", "0");
        assertNotNull(versionZeroExists);

        MetaData versionOneMissing = metaDataDao.getByNameAndVersion("test", "1");
        assertNull(versionOneMissing);
    }

    @Test
    public void testGetByNameReturnsLatestVersion() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);
        MetaData metadata = metaDataDao.getByName("test");
        assertEquals("0", metadata.getVersionNumber());

        metaDataDao.persist(metaData);
        metadata = metaDataDao.getByName("test");
        assertEquals("1", metadata.getVersionNumber());
    }

    @Test
    public void testGetByNameAndVersionReturnsSpecificVersion() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);
        metaDataDao.persist(metaData);

        MetaData metadata = metaDataDao.getByNameAndVersion("test", "0");
        assertEquals("0", metadata.getVersionNumber());

        metadata = metaDataDao.getByNameAndVersion("test", "1");
        assertEquals("1", metadata.getVersionNumber());
    }

    @Test
    public void testDeleteRemovesMetaData() {
        MetaData metaData = new MetaData();
        metaData.setName("test");

        metaDataDao.persist(metaData);

        MetaData metadata = metaDataDao.getByName("test");
        assertNotNull(metadata);

        metaDataDao.delete(metaData);

        metadata = metaDataDao.getByName("test");
        assertNull(metadata);
    }

    @Test
    public void testUpdateMetaDetaWithDuplicates() {
        MetaData mdata2 = new MetaData();
        mdata2.setName("testM2");
        MetaData mnested = new MetaData();
        mnested.setName("testM2_v1");
        mnested.setDataUri("file://testM2_v1/bla/bla");
        mdata2.addDuplicate(mnested);
        MetaData mnested2 = new MetaData();
        mnested2.setName("testM2_v2");
        mnested2.setDataUri("file://testM2_v2/bla/bla");
        mdata2.addDuplicate(mnested2);
        MetaData mnested3 = new MetaData();
        mnested3.setName("testM2_v3");
        mnested3.setDataUri("file://testM2_v3/bla/bla");
        metaDataDao.persist(mdata2);

        mdata2 = metaDataDao.getByName("testM2");
        mdata2.addDuplicate(mnested3);

        metaDataDao.update(mdata2);

        MetaData mtest = metaDataDao.getByName("testM2");

        List<MetaData> metaData = mtest.getDuplicates();

        assertEquals("testM2_v1", metaData.get(0).getName());
        assertEquals("testM2_v2", metaData.get(1).getName());
    }
}
