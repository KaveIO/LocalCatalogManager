package nl.kpmg.lcm.server.data.dao.mongo;

import java.util.LinkedList;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.data.dao.UserDao;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;

public class MongoTaskScheduleDaoTest extends LCMBaseTest {

    @Autowired
    TaskScheduleDao taskScheduleDao;

    @Test
    public void testFindLast() throws UserPasswordHashException {
        String firstId = createTaskSchedule();
        TaskSchedule first = taskScheduleDao.findFirstByOrderByIdDesc();
        assertEquals(firstId, first.getId());

        String secondId = createTaskSchedule();
        TaskSchedule second = taskScheduleDao.findFirstByOrderByIdDesc();
        assertEquals(secondId, second.getId());
    }

    private String createTaskSchedule() {
        TaskSchedule taskSchedule = new TaskSchedule();
        LinkedList items = new LinkedList();
        TaskSchedule.TaskScheduleItem taskScheduleItem = new TaskSchedule.TaskScheduleItem();
        items.add(taskScheduleItem);
        taskSchedule.setItems(items);

        TaskSchedule saved = taskScheduleDao.save(taskSchedule);
        return saved.getId();
    }
}
