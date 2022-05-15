package com.aalto.myBBS;

import static org.junit.Assert.*;
import com.aalto.myBBS.dao.DiscussPostMapper;
import com.aalto.myBBS.dao.LoginTicketMapper;
import com.aalto.myBBS.dao.MessageMapper;
import com.aalto.myBBS.dao.UserMapper;
import com.aalto.myBBS.service.entity.LoginTicket;
import com.aalto.myBBS.service.entity.Message;
import com.aalto.myBBS.service.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MyBbsApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectByEmail() {
        User user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testSelectByName() {
        User user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("Jimmy");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@hotmail.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "1q2w3e");
        System.out.println(rows);
    }
    
    @Test
    public void testDiscussPostMapper() {
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testUpdateStatus() {
        loginTicketMapper.updateStatus("abc", 1);
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    /**
     * This is used for testing the messgaeMapper
     */
    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        assertEquals(14, list.size());

        int count = messageMapper.selectConversationCount(111);
        assertEquals(14, count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        assertEquals(8, list.size());

        count = messageMapper.selectLetterCount("111_112");
        assertEquals(8, count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        assertEquals(2, count);
    }
}
