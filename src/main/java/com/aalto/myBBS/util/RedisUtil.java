package com.aalto.myBBS.util;

public class RedisUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY = "like:entity";

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
}
