package com.aalto.myBBS.dao;

import com.aalto.myBBS.service.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // entityType为实体类型，entityId为实体Id， offset和limit用于分页。
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment (Comment comment);
}
