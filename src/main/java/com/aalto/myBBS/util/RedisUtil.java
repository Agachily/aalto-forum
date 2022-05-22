package com.aalto.myBBS.util;

public class RedisUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee"; // Entity that you have followed
    private static final String PREFIX_FOLLOWER = "follower"; // Entity that follows you

    /**
     * This function is used to generate a key for a certain entity, in the form
     * like:entity:entityType:entityId
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getKey(int entityType, int entityId) {
        return PREFIX_ENTITY + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * This function is used to generate a certain for a certain user.
     * @param userId
     * @return
     */
    public static String getKeyForUser(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * The entities that a user following, int the form followee:userId:entityType -> zset(entityId, currentTime)
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * The follower that an entity possesses, in the form follower:entityType:entityId -> zset(userId, currentTime)
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityId + SPLIT + entityType;
    }
}
