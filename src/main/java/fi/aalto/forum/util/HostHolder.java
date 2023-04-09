package fi.aalto.forum.util;

import fi.aalto.forum.entity.User;
import org.springframework.stereotype.Component;

/**
 * This class is used to hold the user information, functions as a session object
 */
@Component
public class HostHolder {

    /* Use TheadLocal to store data that can be accessible only by a specific thread */
    private final ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    /* Clear the storage after finishing the HTTP request */
    public void clear() {
        users.remove();
    }
}
