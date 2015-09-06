package nl.kpmg.lcm.server.mongodb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.dao.file.MetaDataDaoImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MongoDbMetaDataDaoImplTest {
	@Mock
	MetaDataDao metaDataDao;

	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetByName() {
		metaDataDao = mock(MetaDataDaoImpl.class);

		MetaData md = new MetaData();
		md.set("user", "abc");

		when(metaDataDao.getByName("user")).thenReturn(md);
		MetaData md1 = metaDataDao.getByName("user");
		assertEquals(md1.get("user").toString(),md.get("user"));
	}

}
