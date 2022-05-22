package com.aalto.myBBS.controller;

import com.aalto.myBBS.annotation.LoginRequired;
import com.aalto.myBBS.service.GiveLikeService;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.service.UserService;
import com.aalto.myBBS.util.HostHolder;
import com.aalto.myBBS.util.MybbsUtil;
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
public class UserController {
    // Define the logger for recording
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${mybbs.path.domain}")
    private String domain;

    @Value("${mybbs.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private GiveLikeService giveLikeService;

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
     * This method is used to upload the header photo
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "You do not choose the picture yet");
            return "/site/setting";
        }

        /* Generate a random name for the picture */
        // Get the original file name
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "The file format you uploaded is not correct");
            return "/site/setting";
        }
        // Generate the original filename
        fileName = MybbsUtil.generateUUID() + "." + suffix;
        // Set the path to store the file
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // Store the file
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("Fail to upload the picture");
            throw new RuntimeException("Fail to upload the picture, there are something wrong with the server");
        }

        // Update the path of the header image for the current user to visit
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        // Return the index page
        return "redirect:/index";
    }

    /**
     * Get the header image
     * @return
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

        // 设置用户
        model.addAttribute("user", user);
        // 点赞数量
        int userLikeNumber = giveLikeService.findUserLikeNumber(userId);
        model.addAttribute("likeNumber", userLikeNumber);

        return "/site/profile";
    }

}
