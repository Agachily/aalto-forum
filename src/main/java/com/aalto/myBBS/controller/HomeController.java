package com.aalto.myBBS.controller;

import com.aalto.myBBS.service.GiveLikeService;
import com.aalto.myBBS.service.entity.DiscussPost;
import com.aalto.myBBS.service.entity.Page;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.service.DiscussPostService;
import com.aalto.myBBS.service.UserService;
import com.aalto.myBBS.util.MybbsConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements MybbsConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private GiveLikeService giveLikeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null) {
            for(DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                // Store the post data and the corresponding user into the map object
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                map.put("post", post);
                // Check how many like has been given to that post
                map.put("likeNumber", giveLikeService.findLikeNumberOfEntity(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        model.addAttribute("page", page);
        return "/index";
    }


    /**
     * 用于获取错误页面
     * @return
     */
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    /**
     * 权限不足是返回404页面
     * @return
     */
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
