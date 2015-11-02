package nl.kpmg.lcm.server.data.service;

import org.springframework.beans.factory.annotation.Autowired;

import nl.kpmg.lcm.server.data.dao.UserDao;

/**
 * User Authentication Service
 *
 * @author venkateswarlub
 *
 */
public class UserService {

    @Autowired
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
