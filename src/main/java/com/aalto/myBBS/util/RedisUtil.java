package com.aalto.myBBS.util;

public class RedisUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

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
}
