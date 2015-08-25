package nl.kpmg.lcm.server.data.storage.file;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.backend.BackendHDFSImpl;
import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.file.BackendDaoImpl;
import nl.kpmg.lcm.server.data.service.BackendService;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author kos
 */
public class BackendModelTest extends LCMBaseTest {

    @Autowired
    private BackendDaoImpl backEndDao;

    @Autowired
    private BackendService backendService;

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
