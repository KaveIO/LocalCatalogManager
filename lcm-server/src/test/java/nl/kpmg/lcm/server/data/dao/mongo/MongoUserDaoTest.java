package nl.kpmg.lcm.server.data.dao.mongo;

import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.data.dao.UserDao;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;

public class MongoUserDaoTest extends LCMBaseTest {

    @Autowired
    UserDao userDao;

    @Test
    public void testSaveHashesPassword() throws UserPasswordHashException {
        User expected = new User();
        expected.setName("testUser");
        expected.setPassword("testPassword");

        assertFalse(expected.isHashed());
        userDao.save(expected);

        User actual = userDao.findOneByName("testUser");
        assertTrue(actual.isHashed());
        assertFalse(actual.getPassword().equals("testPassword"));
        assertTrue(actual.passwordEquals("testPassword"));
    }
}
