package com.aalto.myBBS.controller;

import com.aalto.myBBS.service.CommentService;
import com.aalto.myBBS.service.entity.Comment;
import com.aalto.myBBS.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * hostHolder变量用于获取当前用户Id
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 添加Comment
     * @param id
     * @param comment 由Post中的内容获取，Spring MVC会自动根据Post中的内容构建一个comment对象
     * @return
     */
    @RequestMapping(path = "/add/{id}", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") int id, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 依旧跳转到当前帖子页面
        return "redirect:/discuss/detail/" + id;
    }

}
