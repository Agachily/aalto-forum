package fi.aalto.forum.util;

public interface ForumConstant {
    /* Successful Registration */
    int ACTIVATION_SUCCESS = 0;

    /* Repeat Registration */
    int ACTIVATION_REPEAT = 1;

    /* Repeat Failure */
    int ACTIVATION_FAILURE = 2;

    /* Default expiration time */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /* The expiration time in the rememberMe status */
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
     * The ID of system user
     */
    int SYSTEM_USER_ID = 1;

    /**
     * Authority: normal user
     */
    String AUTHORITY_USER = "user";

    /**
     * Authority: admin
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * Authority: moderator
     */
    String AUTHORITY_MODERATOR = "moderator";
}
