package nl.kpmg.lcm.server.data.storage.file;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.backend.BackendHDFSImpl;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.file.StorageDaoImpl;
import nl.kpmg.lcm.server.data.service.StorageService;
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
    private StorageDaoImpl storageDao;

    @Autowired
    private StorageService storageService;

    @Ignore
    @Test
    public void testFetchConfiguredBackendByName() throws DaoException {
        Storage storage1 = new Storage();
        storage1.setName("test");
        storage1.setOptions(new HashMap());
        storage1.getOptions().put("storagePath", "/tmp/");

        Storage storage2 = new Storage();
        storage2.setName("test2");
        storage2.setOptions(new HashMap());
        storage2.getOptions().put("storagePath", "/tmp2/");

        Storage storage3 = new Storage();
        storage3.setName("test3");
        storage3.setOptions(new HashMap());
        storage3.getOptions().put("storagePath", "/tmp3/");

        Storage storage4 = new Storage();
        storage4.setName("test3");
        storage4.setOptions(new HashMap());
        storage4.getOptions().put("storagePath", "/tmp3b/");

        storageDao.persist(storage1);
        storageDao.persist(storage2);
        storageDao.persist(storage3);
        storageDao.persist(storage4);

        List<Storage> backEndList;
        backEndList = storageDao.getAll();

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

        backend = storageService.getBackend("file://test3/bla/bla");

        assertEquals(BackendFileImpl.class, backend.getClass());

        BackendFileImpl backendImpl = (BackendFileImpl) backend;
        assertEquals(new File("/tmp3b/"), backendImpl.getStoragePath());
    }

    @Test
    public void testFetchandConfigureHDFS() throws DaoException {
        Storage storage = new Storage();
        storage.setName("testHDFS");
        storage.setOptions(new HashMap());
        storage.getOptions().put("storagePath", "/tmpHDFS/");
        storageDao.persist(storage);
        Backend backendHDFS;

        backendHDFS = storageService.getBackend("hdfs://testHDFS/bla/bla");

        assertEquals(BackendHDFSImpl.class, backendHDFS.getClass());

        BackendHDFSImpl backendImpl = (BackendHDFSImpl) backendHDFS;
        assertEquals("/tmpHDFS/", backendImpl.getStoragePath());

        storageDao.delete(storage);
    }
}
