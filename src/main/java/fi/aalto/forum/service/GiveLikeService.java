package fi.aalto.forum.service;

import fi.aalto.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class GiveLikeService {

    private final RedisTemplate redisTemplate;

    public GiveLikeService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * The method implements the service of giving / canceling like to a certain entity.
     * We use Set for storing the data. The userId of the people who gives like will be
     * stored in the set.
     * Giving like to a certain entity is treated as giving like to the user who create the entity.
     * @param entityUserId The auther of this entity(Post or Comment)
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
     */
    public long findLikeNumberOfEntity(int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * Check whether somebody has given like to a certain entity
     */
    public int checkLikeStatusOfEntity(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     * Check the likes have been given to a certain user.
     */
    public int findUserLikeNumber(int userId) {
        String keyForUser = RedisUtil.getKeyForUser(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(keyForUser);
        return count == null ? 0 : count;
    }

}
