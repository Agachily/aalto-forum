package fi.aalto.forum.service;

import fi.aalto.forum.dao.UserMapper;
import fi.aalto.forum.entity.LoginTicket;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.util.MailClient;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
import fi.aalto.forum.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements ForumConstant {

    private final UserMapper userMapper;
    private final MailClient mailClient;
    private final TemplateEngine templateEngine;
    private final RedisTemplate redisTemplate;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public UserService(UserMapper userMapper, MailClient mailClient, TemplateEngine templateEngine, RedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
        this.redisTemplate = redisTemplate;
    }

    public User findUserById(int id) {
        /* Try to get user from cache(redis), is failed, get user object and put it to cache */
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }


    /**
     * Deal the registration service
     * @return A map. If the map is empty, denoting the registration success.
     */
    public Map<String, Object> register(User user) {
        HashMap<String , Object> map = new HashMap<>();

        /* Check whether the passed in param is valid*/
        if (user == null) {
            throw new IllegalArgumentException("The 'user' param should not be null.");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "The username field should not be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "The password field should not be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "The email field should not be empty.");
            return map;
        }

        /* Check whether the username is in use */
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "The username is already in use, please use another one.");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "The email address is already in use.");
            return map;
        }

        /* Register the user */
        user.setSalt(ForumUtil.generateUUID().substring(0, 5));
        user.setPassword(ForumUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(ForumUtil.generateUUID());
        /* Set user a default head photo (http://images.nowcoder.com/head/(1-1000)t.png) */
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        /* insert the user into database, and the Id field wil be added automatically after inserting the user */
        userMapper.insertUser(user);

        /* Send user the activation Email */
        Context context = new Context();
        /* Set the email variable to be passed to the page */
        context.setVariable("email", user.getEmail());
        /* Set the url for activation, like http://localhost:8080/forum/activation/userid/code */
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        /* Convert the Thymeleaf format to HTML format */
        String content = templateEngine.process("/mail/activation", context);
        /* Send the Email */
        mailClient.sendMail(user.getEmail(), "Activate Your Account", content);

        /* If the registration is successful, the map should be empty */
        return map;
    }

    /**
     * Deal with the activation service
     * @return Return the status of activation
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        /* If the status is 1, denoting the user has been activated */
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            /* We need to set the status to be 1 to denote the success of activation */
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        /* Deal with the empty value */
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "The username should not be empty");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "The password should not be empty");
        }

        /* Verify the account */
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "The account doesn't exist");
            return map;
        }

        /* Verify the password */
        password = ForumUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "The password is not correct");
            return map;
        }

        /* If login success, generate the login ticket */
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(ForumUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        String ticketKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        /* Stored the ticket to redis */
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        String ticketKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * About the information of user header
     */
    public void updateHeader(int userId, String headerUrl) {
        int updateRows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
    }

    public Map<String, Object> resetPassword(User user, String oldpassword, String newpassword) {
        Map<String, Object> map = new HashMap<>();

        oldpassword = ForumUtil.md5(oldpassword + user.getSalt());
        if (!user.getPassword().equals(oldpassword)) {
            map.put("passwordMsg", "The original password is not correct");
            return map;
        }
        newpassword = ForumUtil.md5(newpassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newpassword);
        return null;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * Get the currently logged-in user from cache. namely redis
     */
    private User getCache(int userId) {
        String userKey = RedisUtil.getUserKey(userId);
        /* The JSON string will be converted to the User object */
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 2, TimeUnit.HOURS);
        return user;
    }

    private void clearCache(int userId) {
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     * Get the authorities of the speficied user
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
