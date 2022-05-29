package com.aalto.myBBS.util;

public interface MybbsConstant {
    /* Successful Registration */
    int ACTIVATION_SUCCESS = 0;

    /* Repeat Registration */
    int ACTIVATION_REPEAT = 1;

    /* Repeat Failure */
    int ACTIVATION_FAILURE = 2;

    /* Default expiration time */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * The expiration time int he rememberMe status
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * Entity type: user
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * Topic type: comment
     */
    String TOPIC_COMMENT = "comment";

    /**
     * Topic type: like
     */
    String TOPIC_LIKE = "like";

    /**
     * Topic type: follow
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * Topic type: publish
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * Topic type: delete
     */
    String TOPIC_DELETE = "delete";

    /**
     * The Id of system user
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";
}
