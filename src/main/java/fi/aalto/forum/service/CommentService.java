package fi.aalto.forum.service;

import fi.aalto.forum.dao.CommentMapper;
import fi.aalto.forum.entity.Comment;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements ForumConstant {

    private final CommentMapper commentMapper;
    private final DiscussPostService discussPostService;
    private final SensitiveFilter sensitiveFilter;

    public CommentService(CommentMapper commentMapper, DiscussPostService discussPostService, SensitiveFilter sensitiveFilter) {
        this.commentMapper = commentMapper;
        this.discussPostService = discussPostService;
        this.sensitiveFilter = sensitiveFilter;
    }

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * The method for adding comment, and notice it needs to use transaction to manage to procedure
     * for adding data.
     * @param comment The comment entity to be added
     */
    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("The comment parameter should not be null");
        }

        /* Filter the content in the comment entity */
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        /* If the comment object here representing the comments to a post rather than another comment, update
        * the number of the post comment */
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
