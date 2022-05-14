package com.aalto.myBBS.controller;

import com.aalto.myBBS.service.entity.DiscussPost;
import com.aalto.myBBS.service.entity.Page;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.service.CommentService;
import com.aalto.myBBS.service.DiscussPostService;
import com.aalto.myBBS.service.UserService;
import com.aalto.myBBS.util.HostHolder;
import com.aalto.myBBS.util.MybbsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    // To return the JSON string, we should use the @RequestBody annotation and the String as the return type.
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return MybbsUtil.getJSONString(403, "Login required");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // The error will be processed in the future
        return MybbsUtil.getJSONString(200, "The post has been released");
    }

    // 注意此处返回的是模版，所以不需要写@ResponseBody
    // 注意如果参数中传入了Java Bean类型的参数，Spring MVC会自动将其存入到Model中
    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("id") int id, Model model, Page page) {
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);

        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        return "/site/discuss-detail";
    }
}
