package com.aalto.myBBS.service;

import com.aalto.myBBS.dao.LoginTicketMapper;
import com.aalto.myBBS.dao.UserMapper;
import com.aalto.myBBS.service.entity.LoginTicket;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.util.MailClient;
import com.aalto.myBBS.util.MybbsConstant;
import com.aalto.myBBS.util.MybbsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements MybbsConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${mybbs.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id) {
        return userMapper.selectById(id);
    }


    /**
     * Deal the registration service
     * @param user
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

        /* Check whether the passed in param is in use */
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
        user.setSalt(MybbsUtil.generateUUID().substring(0, 5));
        user.setPassword(MybbsUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(MybbsUtil.generateUUID());
        // Set user a default head photo (http://images.nowcoder.com/head/(1-1000)t.png)
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        // insert the user into database
        userMapper.insertUser(user);

        /* Send user the activation Email */
        Context context = new Context();
        // Set the email field in the page
        context.setVariable("email", user.getEmail());
        // Set the url for activation, like http://localhost:8080/mybbs/activation/userid/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        // Send the Email
        mailClient.sendMail(user.getEmail(), "Activate Your Account", content);

        // If the registration is successful, the map should be empty
        return map;
    }

    /**
     * Deal with the activation service
     * @param userId
     * @param code
     * @return Return the status of activation
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        // If the status is 1, denoting the user has been activated
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // We need to set the status to be 1 to denote the success of activation
            userMapper.updateStatus(userId, 1);
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
        password = MybbsUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "The password is not correct");
            return map;
        }

        /* Generate the login ticket */
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(MybbsUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    /**
     * About the information of user header
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    public Map<String, Object> resetPassword(User user, String oldpassword, String newpassword) {
        Map<String, Object> map = new HashMap<>();

        oldpassword = MybbsUtil.md5(oldpassword + user.getSalt());
        if (!user.getPassword().equals(oldpassword)) {
            map.put("passwordMsg", "The original password is not correct");
            return map;
        }
        newpassword = MybbsUtil.md5(newpassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newpassword);
        return null;
    }
}
