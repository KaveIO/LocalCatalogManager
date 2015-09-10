package nl.kpmg.lcm.server;

import java.io.File;
import org.junit.After;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/application-context.xml", "/application-context-file.xml"})
public abstract class LCMBaseTest {

    protected static final String TEST_STORAGE_PATH = "test";

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
        file = new File(TEST_STORAGE_PATH + "/storage");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/users");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/userGroups");
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
        file = new File(TEST_STORAGE_PATH + "/users");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/userGroups");
        file.delete();
    }

    @After
    public void tearDown() {
        File file = new File(TEST_STORAGE_PATH);
        for (File storageMainFolder : file.listFiles()) {
            for (File storageItem : storageMainFolder.listFiles()) {
                if (storageItem.isDirectory()) {
                    for (File versionFile : storageItem.listFiles()) {
                        versionFile.delete();
                    }
                    storageItem.delete();
                } else if (storageItem.isFile()) {
                    storageItem.delete();
                }
            }
        }
    }
}
