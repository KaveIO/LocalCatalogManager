package nl.kpmg.lcm.server.data.storage.file;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.backend.BackendHDFSImpl;
import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.file.BackendDaoImpl;
import nl.kpmg.lcm.server.data.service.BackendService;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author kos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/application-context-file.xml"})
public class BackendModelTest {

    private static final String TEST_STORAGE_PATH = "test";

    @Autowired
    private BackendDaoImpl backEndDao;

    @Autowired
    private BackendService backendService;

    @BeforeClass
    public static void setUpClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.mkdir();
    }

    @AfterClass
    public static void tearDownClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.delete();
    }

    @After
    public void tearDown() {
        File file = new File(TEST_STORAGE_PATH + "/backend");
        for (File backendFile : file.listFiles()) {
            backendFile.delete();
        }
    }

    @Ignore
    @Test
    public void testFetchConfiguredBackendByName() throws DaoException {
        BackendModel backendmodel = new BackendModel();
        backendmodel.setName("test");
        backendmodel.setOptions(new HashMap());
        backendmodel.getOptions().put("storagePath", "/tmp/");

        BackendModel backendmodel2 = new BackendModel();
        backendmodel2.setName("test2");
        backendmodel2.setOptions(new HashMap());
        backendmodel2.getOptions().put("storagePath", "/tmp2/");

        BackendModel backendmodel3 = new BackendModel();
        backendmodel3.setName("test3");
        backendmodel3.setOptions(new HashMap());
        backendmodel3.getOptions().put("storagePath", "/tmp3/");

        BackendModel backendmodel4 = new BackendModel();
        backendmodel4.setName("test3");
        backendmodel4.setOptions(new HashMap());
        backendmodel4.getOptions().put("storagePath", "/tmp3b/");

        backEndDao.persist(backendmodel);
        backEndDao.persist(backendmodel2);
        backEndDao.persist(backendmodel3);
        backEndDao.persist(backendmodel4);

        List<BackendModel> backEndList;
        backEndList = backEndDao.getAll();

        String path1 = (String) backEndList.get(0).getOptions().get("storagePath");
        assertEquals("/tmp/", path1);

        assertEquals("test", backEndList.get(0).getName());

        String path2 = (String) backEndList.get(1).getOptions().get("storagePath");
        assertEquals("/tmp2/", path2);

        assertEquals("test2", backEndList.get(1).getName());

        String path3 = (String) backEndList.get(2).getOptions().get("storagePath");
        assertEquals("/tmp3b/", path3);

        assertEquals("test3", backEndList.get(2).getName());

        Backend backend;

        backend = backendService.getBackend("file://test3/bla/bla");

        assertEquals(BackendFileImpl.class, backend.getClass());

        BackendFileImpl backendImpl = (BackendFileImpl) backend;
        assertEquals(new File("/tmp3b/"), backendImpl.getStoragePath());
    }

    @Test
    public void testFetchandConfigureHDFS() throws DaoException {
        BackendModel backendmodel = new BackendModel();
        backendmodel.setName("testHDFS");
        backendmodel.setOptions(new HashMap());
        backendmodel.getOptions().put("storagePath", "/tmpHDFS/");
        backEndDao.persist(backendmodel);
        Backend backendHDFS;

        backendHDFS = backendService.getBackend("hdfs://testHDFS/bla/bla");

        assertEquals(BackendHDFSImpl.class, backendHDFS.getClass());

        BackendHDFSImpl backendImpl = (BackendHDFSImpl) backendHDFS;
        assertEquals("/tmpHDFS/", backendImpl.getStoragePath());

        backEndDao.delete(backendmodel);
    }
}
