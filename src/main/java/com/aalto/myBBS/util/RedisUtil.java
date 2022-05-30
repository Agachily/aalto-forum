package com.aalto.myBBS.util;

public class RedisUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee"; // Entity that you have followed
    private static final String PREFIX_FOLLOWER = "follower"; // Entity that follows you
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

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

    /**
     * 生成用于验证码的key
     * @param ticket 给客户端的临时凭证
     * @return
     */
    public static String getKaptchaKay(String ticket) {
        return PREFIX_KAPTCHA + SPLIT + ticket;
    }

    /**
     *
     * @param ticket The ticket the user carrying when try to login
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * Get the key to identity the user
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 生成一个UV Key，用于记录单日数据
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间UV
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 用于统计单日活跃用户的key
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 用于统计一个区间内活跃用户的key
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT +endDate;
    }
}
