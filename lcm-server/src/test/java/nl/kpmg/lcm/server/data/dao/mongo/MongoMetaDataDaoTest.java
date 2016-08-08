package nl.kpmg.lcm.server.data.dao.mongo;

import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.springframework.beans.factory.annotation.Autowired;

public class MongoMetaDataDaoTest extends LCMBaseTest {

    @Autowired
    MetaDataDao metaDataDao;

    @Test
    public void testSave() {
        String expectedName = "test";
        String expectedUri = "file://test/test";

        MetaData metaData = new MetaData();
        metaData.setName(expectedName);
        metaData.setDataUri(expectedUri);

        MetaData saved = metaDataDao.save(metaData);

        MetaData actual = metaDataDao.findOne(saved.getId());

        assertFalse(actual == metaData);
        assertEquals(expectedName, actual.getName());
        assertEquals(expectedUri, actual.getDataUri());
    }
}
