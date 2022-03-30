package com.aalto.myBBS.util;

import com.aalto.myBBS.entity.User;
import org.springframework.stereotype.Component;

/**
 * This class is used to hold the user information
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    // Clear the storage after finish the HTTP request
    public void clear() {
        users.remove();
    }
}
