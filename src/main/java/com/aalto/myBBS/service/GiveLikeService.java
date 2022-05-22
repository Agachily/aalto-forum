package com.aalto.myBBS.service;

import com.aalto.myBBS.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class GiveLikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * The method implements the service of giving / canceling like to a certain entity.
     * We use set for storing the data. The userId of the people who gives like will be
     * stored in the set. 在对某一个实体点赞的时候同时也视为对该实体的用户点赞。
     * @param userId
     * @param entityType
     * @param entityId
     * @param entityUserId 该实体（帖子 / 评论）的作者
     */
    public void giveOrCancelLike(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityKey = RedisUtil.getKey(entityType, entityId);
                String userKey = RedisUtil.getKeyForUser(entityUserId);

                // 判断当前用户是否已经对该实体点过赞了，如果已经点过赞了，那么再次点击则视为取消
                Boolean isMember = operations.opsForSet().isMember(entityKey, userId);
                // 开启事务
                operations.multi();
                if (isMember) {
                    operations.opsForSet().remove(entityKey, userId);
                    operations.opsForValue().decrement(userKey);
                } else {
                    operations.opsForSet().add(entityKey, userId);
                    operations.opsForValue().increment(userKey);
                }
                return operations.exec();
            }
        });
    }

    /**
     * Get the number of like that has been given to a certain entity
     * @param entityType
     * @param entityId
     * @return
     */
    public long findLikeNumberOfEntity(int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * Check whether somebody has given like to a certain entity
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int checkLikeStatusOfEntity(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞的数量
    public int findUserLikeNumber(int userId) {
        String keyForUser = RedisUtil.getKeyForUser(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(keyForUser);
        return count == null ? 0 : count.intValue();
    }
}
