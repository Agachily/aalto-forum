package com.aalto.myBBS;

import com.aalto.myBBS.dao.DiscussPostMapper;
import com.aalto.myBBS.dao.UserMapper;
import com.aalto.myBBS.entity.DiscussPost;
import com.aalto.myBBS.entity.User;
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
    private DiscussPostMapper discussPostMapper;

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
        /*List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost d : discussPosts) {
            System.out.println(d);
        }*/

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
}
