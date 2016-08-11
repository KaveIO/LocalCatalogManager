package nl.kpmg.lcm.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/application-context-server-mongo.xml"})
@DirtiesContext
public abstract class LCMBaseTest {

  private static DatabaseInitialiser databaseInitialiser;

  @BeforeClass
  public static void setUpClass() throws Exception {
    databaseInitialiser = new DatabaseInitialiser();
    databaseInitialiser.start();
  }

  @AfterClass
  public static void tearDownClass() {
    databaseInitialiser.stop();
  }
}
