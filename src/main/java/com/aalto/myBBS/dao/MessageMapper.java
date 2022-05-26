package com.aalto.myBBS.dao;

import com.aalto.myBBS.service.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信，并且按照id倒序排列
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询当前用户所有未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息状态，将消息状态从未读设置为已读
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的系统通知，即fromId为1的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的系统通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的系统·通知的数量
    int selectNoticeCountUnread(int userId, String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
