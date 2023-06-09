package fi.aalto.forum.controller;

import fi.aalto.forum.annotation.LoginRequired;
import fi.aalto.forum.service.FollowService;
import fi.aalto.forum.service.GiveLikeService;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.util.HostHolder;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${forum.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final UserService userService;

    private final HostHolder hostHolder;

    private final GiveLikeService giveLikeService;

    private final FollowService followService;

    public UserController(UserService userService, HostHolder hostHolder, GiveLikeService giveLikeService, FollowService followService) {
        this.userService = userService;
        this.hostHolder = hostHolder;
        this.giveLikeService = giveLikeService;
        this.followService = followService;
    }

    /**
     * Define the method to response the user with the setting page
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * This method is used to upload the header photo for user
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "You do not choose the picture yet");
            return "/site/setting";
        }

        /* Generate a random name for the picture */
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "The file format you uploaded is not correct");
            return "/site/setting";
        }
        fileName = ForumUtil.generateUUID() + "." + suffix;
        /* Set the path to store the file */
        File dest = new File(uploadPath + "/" + fileName);
        try {
            /* Store the file */
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("Fail to upload the picture");
            throw new RuntimeException("Fail to upload the picture, there are something wrong with the server");
        }

        /* Update the path of the header image for the current user to visit */
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * Get the header image
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // Construct the file path according to the url
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        response.setContentType("/image" + suffix);
        try (FileInputStream fis = new FileInputStream(fileName);) {
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0 , b);
            }
        } catch (IOException e) {
           logger.error("Fail to read the header image" + e.getMessage());
        }
    }

    /**
     * reset the password
     */
    @RequestMapping(path="/resetpassword", method=RequestMethod.POST)
    public String resetPassword(String oldpassword, String newpassword, String confirmpassword, Model model) {
        if (!confirmpassword.equals(newpassword)) {
            model.addAttribute("confirmerror", "The password you input is different");
            model.addAttribute("oldpassword", oldpassword);
            model.addAttribute("newpassword", newpassword);
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        Map<String, Object> map = userService.resetPassword(user, oldpassword, newpassword);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "The password is updated successfully");
            model.addAttribute("target", "/logout");
            return "/site/operate-result";
        } else {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/setting";
        }
    }

    // 访问个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("The user does not exist");
        }

        /* Set user to the model */
        model.addAttribute("user", user);
        // 点赞数量
        int userLikeNumber = giveLikeService.findUserLikeNumber(userId);
        model.addAttribute("likeNumber", userLikeNumber);
        // 关注用户数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前登陆的用户是否已经关注该用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
