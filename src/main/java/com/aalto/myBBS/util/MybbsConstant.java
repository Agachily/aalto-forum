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
     * The entity type of post
     */

}
