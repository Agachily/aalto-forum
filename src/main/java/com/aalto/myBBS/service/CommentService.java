package com.aalto.myBBS.service;

import com.aalto.myBBS.dao.CommentMapper;
import com.aalto.myBBS.service.entity.Comment;
import com.aalto.myBBS.util.MybbsConstant;
import com.aalto.myBBS.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements MybbsConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset,int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论，注意，该方法要用事务进行管理
     * @param comment 用于添加的评论实体
     * @return 受影响的行数
     */
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("The comment parameter should not be null");
        }

        // 对Comment中的内容进行过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 如果comment是帖子的评论，则更新帖子评论的数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }
}
