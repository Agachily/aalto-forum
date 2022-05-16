package com.aalto.myBBS.service;

import com.aalto.myBBS.dao.MessageMapper;
import com.aalto.myBBS.service.entity.Message;
import com.aalto.myBBS.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 添加私信
     * @param message
     * @return
     */
    public int addMessage(Message message) {
        // 在插入信息之前首先对其进行过滤
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 查看信息，并将信息状态设置为已读
     * @param ids
     * @return
     */
    public int checkMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }
}
