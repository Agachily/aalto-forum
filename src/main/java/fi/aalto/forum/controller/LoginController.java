package fi.aalto.forum.controller;

import fi.aalto.forum.entity.User;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
import fi.aalto.forum.util.RedisUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Deal with the login function
 */
@Controller
public class LoginController implements ForumConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private UserService userService;
    private Producer kaptchaProducer;
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public LoginController(UserService userService, Producer kaptchaProducer, RedisTemplate redisTemplate) {
        this.userService = userService;
        this.kaptchaProducer = kaptchaProducer;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get the page for registration
     * @return The register.html page
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        /* Return the register.html under the folder templates/static for user registration */
        return "/site/register";
    }

    /**
     * Process the user data for registration
     * @return The operate-result.html page if succeed or the register.html page if fail
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        /* The user object received the posted data */
        Map<String, Object> map = userService.register(user);

        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "An activation Email is sent, please check in the registered Email");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            /* If the registration fails, return the original registration page,
               Remember the user object will be put into spring mvc automatically */
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "The activation is successful");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "The activation is invalid, the account id already in use");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "The activation fails, the link is not correct");
            model.addAttribute("target", "/index");
        }

        return "/site/operate-result";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        /* Return the register.html under the folder templates/static */
        return "/site/login";
    }

    /**
     * This endpoint is used to return a authorisation picture for the user
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        /* Generate the code */
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        /* Generate a temporary to identify the client when verify the activation code */
        String ticket = ForumUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaTicket", ticket);
        cookie.setMaxAge(300);
        /* Specifies path for the cookie to which the client should return the cookie. */
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        /* Put the verification code to redis */
        String kaptchaKey = RedisUtil.getKaptchaKey(ticket);
        redisTemplate.opsForValue().set(kaptchaKey, text, 300, TimeUnit.SECONDS); // 300 秒后验证码失效

        /* Send the picture to the browser */
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("Fail to response the code" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(
            String username, String password, String code,
            boolean rememberMe, Model model, HttpServletResponse response,
            @CookieValue(value = "kaptchaTicket", required = false) String kaptchaTicket) {
        /* Get the activation code from the redis */
        String kaptcha = null;
        if (StringUtils.isNoneBlank(kaptchaTicket)) {
            String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaTicket);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        /* Judge whether the activation code is valid or not */
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "The verification code is not right or outdated");
            return "/site/login";
        }

        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        /* Call the userService class to deal with the login issue */
        Map<String, Object> map = userService.login(username, password, expiredSeconds);

        /* Judge if the login is successful */
        if (map.containsKey("ticket")) {
            /* Send the ticket to use client as the cookie */
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            /* Specifies a path for the cookie to which the client should return the cookie.
               The cookie is visible to all the pages in the directory you specify and all the pages in that directory's subdirectories. */
            cookie.setPath(contextPath);
            /* Sets the maximum age of the cookie in seconds. */
            response.addCookie(cookie);
            /* Return to the main page after successful login. */
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        /* Redirect to login page */
        return "redirect:/login";
    }

}
