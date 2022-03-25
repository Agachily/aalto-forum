package com.aalto.myBBS.service;

import com.aalto.myBBS.dao.UserMapper;
import com.aalto.myBBS.entity.User;
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
}
