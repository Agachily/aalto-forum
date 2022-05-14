package com.aalto.myBBS.controller;

import com.aalto.myBBS.service.entity.Comment;
import com.aalto.myBBS.service.entity.DiscussPost;
import com.aalto.myBBS.service.entity.Page;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.service.CommentService;
import com.aalto.myBBS.service.DiscussPostService;
import com.aalto.myBBS.service.UserService;
import com.aalto.myBBS.util.HostHolder;
import com.aalto.myBBS.util.MybbsConstant;
import com.aalto.myBBS.util.MybbsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements MybbsConstant {
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
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        // 目前一共有两种评论，给帖子的评论和给评论的评论
        // 先获取给帖子的评论
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 使用List和Map来组织评论信息，Map中存储评论，用户信息，该评论的回复
        List<Map<String, Object>> commentListToShow = new ArrayList<>();
        if (commentList != null) {
            for (Comment c : commentList) {
                Map<String, Object> commentToShow = new HashMap<>();
                // 评论对象
                commentToShow.put("comment", c);
                // 该评论的作者
                commentToShow.put("user", userService.findUserById(c.getUserId()));

                // 获取与该评论的相关的回复
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, c.getId(), 0, Integer.MAX_VALUE);

                ArrayList<Object> replyListToShow = new ArrayList<>();

                if (replyList != null) {
                    for (Comment r : replyList) {
                        Map<String, Object> replyToShow = new HashMap<>();
                        // 回复
                        replyToShow.put("reply", r);
                        // 作者
                        replyToShow.put("user", userService.findUserById(r.getUserId()));
                        // 回复目标
                        User target = r.getTargetId() == 0 ? null : userService.findUserById(r.getTargetId());
                        replyToShow.put("target", target);

                        replyListToShow.add(replyToShow);
                    }
                }
                commentToShow.put("replies", replyListToShow);

                // 该评论回复的数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, c.getId());
                commentToShow.put("replyCount", replyCount);

                commentListToShow.add(commentToShow);
            }
        }

        // 该帖子下的所有回复都被封装到了commentListToShow对象中，并将该对象放到model中。
        model.addAttribute("comments", commentListToShow);

        return "/site/discuss-detail";
    }
}
