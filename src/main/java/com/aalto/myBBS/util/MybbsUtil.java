package com.aalto.myBBS.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author zetong
 * @description Provide the common tools to facilitate the development of the project
 */
@Component
public class MybbsUtil {
    /**
     * Generate a random string
     * @return A random string
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * Calculate the MD5 hash
     * @return The result of MD5 hash
     */
    public static String md5(String key) {
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
