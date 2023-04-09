package fi.aalto.forum.service;

import fi.aalto.forum.dao.MessageMapper;
import fi.aalto.forum.entity.Message;
import fi.aalto.forum.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    private final MessageMapper messageMapper;

    private final SensitiveFilter sensitiveFilter;

    public MessageService(MessageMapper messageMapper, SensitiveFilter sensitiveFilter) {
        this.messageMapper = messageMapper;
        this.sensitiveFilter = sensitiveFilter;
    }

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

    public void addMessage(Message message) {
        /* Filter the message before adding it */
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        messageMapper.insertMessage(message);
    }

    /**
     * Check the message and set the status to "read"
     */
    public void checkMessage(List<Integer> ids) {
        messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeCountUnread(userId, topic);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

}

