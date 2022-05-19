package com.aalto.myBBS.service;

import com.aalto.myBBS.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GiveLikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * The method implements the service of giving / canceling like to a certain entity.
     * We use set for storing the data. The userId of the people who gives like will be
     * stored in the set.
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void giveOrCancelLike(int userId, int entityType, int entityId) {
        // Get the key for a certain entity
        String entityLikeKey = RedisUtil.getKey(entityType, entityId);
        // Judge whether the user has given like to that entity
        Boolean isGiven = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isGiven) {
            // If the like is given by the user, remove it, corresponding to the canceling operation
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            // If the like is not given by the user, add it
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
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
}
