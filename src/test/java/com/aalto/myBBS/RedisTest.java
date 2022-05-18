package com.aalto.myBBS;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MyBbsApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        int result =  (Integer) redisTemplate.opsForValue().get(redisKey);
        assertEquals(result, 1);
    }

    @Test
    public void testSortedSets() {
        // 操作Redis中的有序集合
        String redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey, "User1", 80);
        redisTemplate.opsForZSet().add(redisKey, "User2", 90);
        redisTemplate.opsForZSet().add(redisKey, "User3", 50);
        redisTemplate.opsForZSet().add(redisKey, "User4", 70);
        redisTemplate.opsForZSet().add(redisKey, "User5", 60);
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "User3"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "User3"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    @Test
    public void testKeys() {
        // 删除某个键
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        // 设置"test:students"键10秒后过期，使用TimeUnit.SECONDS来制定单位
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    @Test
    public void testSets() {
        // 操作Redis中的集合
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "User1", "User2", "User3", "User4", "User5");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testLists() {
        // 操作列表
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testBoundOperations() {
        // 绑定对象，多次访问同一个key，以批量发送命令,节约网络开销.
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // Redis中编程事务控制
    @Test
    public void testTransaction() {
        Object result = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "text:tx";

                // 启用事务
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey, "Test1");
                redisOperations.opsForSet().add(redisKey, "Test2");
                redisOperations.opsForSet().add(redisKey, "Test3");

                // 因此事务还没提交，因此此时查询不会查到数据
                System.out.println(redisOperations.opsForSet().members(redisKey));

                // 提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(result);
    }
}
