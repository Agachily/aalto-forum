package com.aalto.myBBS.controller;

import com.aalto.myBBS.entity.User;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

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

    /**
     * Define the method to response the user with the setting page
     * @return
     */
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * This method is used to upload the header photo
     * @return
     */
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
        fileName = MybbsUtil.generateUUID() + suffix;
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
}
