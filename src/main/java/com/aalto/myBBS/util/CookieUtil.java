package com.aalto.myBBS.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    /**
     * Get the specific cookie from the HttpServletRequest object
     * @param request
     * @param name
     * @return
     */
    public static String getValue(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            throw new IllegalArgumentException("The parameter is empty");
        }

        // Get the set with all the possible cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Return the cookie if the name matches
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    
}
