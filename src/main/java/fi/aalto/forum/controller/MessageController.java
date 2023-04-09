package fi.aalto.forum.controller;

import fi.aalto.forum.annotation.LoginRequired;
import fi.aalto.forum.service.MessageService;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.entity.Message;
import fi.aalto.forum.entity.Page;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.util.HostHolder;
import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements ForumConstant {

    private final MessageService messageService;

    private final UserService userService;

    /* hostHolder is used to get th information of current logged user */
    private final HostHolder hostHolder;

    public MessageController(MessageService messageService, UserService userService, HostHolder hostHolder) {
        this.messageService = messageService;
        this.userService = userService;
        this.hostHolder = hostHolder;
    }

    /**
     * Get the letter list for a certain user
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    @LoginRequired
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表，conversationList中存储了
        List<Message> conversationsList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        // Map用于存储某一个对象和相应的说明信息，其整个被存储到List中
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationsList != null) {
            for (Message message : conversationsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                /* Get the number of unread letter in the current conversation */
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 获取与用户进行通信的User对象
                map.put("target", getLetterTarget(message.getConversationId()));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        /* Get the number of all the notice(system to user) that have not been read */
        int unreadNoticeCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", unreadNoticeCount);
        /* Get the number of all the messages(user to user) that have not been read */
        int unreadMessageCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", unreadMessageCount);

        return "/site/letter";
    }

    /**
     * 显示私信会话的详细信息
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        // 处理letterList中的每一个对象，将其中的信息取出并与其他附属信息一起组装为一个Map对象并放到List集合中
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                // 存储fromUser所对应的信息
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }

        model.addAttribute("letters", letters);

        // 获取与用户进行通信的User对象
        model.addAttribute("target", getLetterTarget(conversationId));

        // 将用户所查看的信息设置为已读
        List<Integer> messageIds = getMessageIds(letterList);
        if (!messageIds.isEmpty()) {
            messageService.checkMessage(messageIds);
        }

        return "/site/letter-detail";
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String receiverName, String content) {
        User receiver = userService.findUserByName(receiverName);
        User currentUser = hostHolder.getUser();
        if (receiver ==  null) {
            return ForumUtil.getJSONString(400, "The receiver does not exist");
        }
        if (receiver.getId() == currentUser.getId()) {
            System.out.println("sada" + content + "asdas");
            return ForumUtil.getJSONString(400, "You should not send letter to yourself");
        }
        if (content.equals("")) {
            return ForumUtil.getJSONString(400, "The content is empty");
        }

        // 构造Message对象
        Message message = new Message();
        message.setFromId(currentUser.getId());
        message.setToId(receiver.getId());
        /* Construct the conversation id in the format userid1_userid2, the smaller one is at the front */
        if (message.getFromId() < message.getToId())
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        else
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);

        return ForumUtil.getJSONString(200, "The message has been sent");
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getMessageIds(List<Message> messageList) {
        List<Integer> ids = new ArrayList<>();

        if (messageList != null) {
            for (Message message : messageList) {
                // 筛选条件有两个，该信息状态是未读的，并且当前用户是发送方。
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论类通知
        Map messageCommentInfo = getMessageInfo(user.getId(), TOPIC_COMMENT);
        if (messageCommentInfo != null)
            model.addAttribute("commentNotice", messageCommentInfo);
        Map messageLikeInfo = getMessageInfo(user.getId(), TOPIC_LIKE);
        if (messageCommentInfo != null)
            model.addAttribute("likeNotice", messageLikeInfo);
        Map messageFollowInfo = getMessageInfo(user.getId(), TOPIC_FOLLOW);
        if (messageCommentInfo != null)
            model.addAttribute("followNotice", messageFollowInfo);

        // 查询所有未读通知数量
        int unreadNoticeCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", unreadNoticeCount);
        // 查询所有未读消息的数量
        int unreadMessageCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", unreadMessageCount);

        return "/site/notice.html";
    }

    private Map getMessageInfo(int userId, String topic) {
        Message message = messageService.findLatestNotice(userId, topic);
        if (message != null) {
            Map<String, Object> messageToShow = new HashMap<>();
            messageToShow.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageToShow.put("user", userService.findUserById((Integer) data.get("userId")));
            messageToShow.put("entityType", data.get("entityType"));
            messageToShow.put("entityId", data.get("entityId"));
            if (!topic.equals(TOPIC_FOLLOW))
                messageToShow.put("postId", data.get("postId"));

            // 查询所有消息的数量
            int count = messageService.findNoticeCount(userId, topic);
            // 查询所有未读消息的数量
            int unread = messageService.findNoticeUnreadCount(userId, topic);
            messageToShow.put("count", count);
            messageToShow.put("unread", unread);

            return messageToShow;
        }
        return null;
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        // Set pagination
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        if (noticeList != null) {
            List<Map<String, Object>> noticeListToShow = new ArrayList<>();
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeListToShow.add(map);
            }
            model.addAttribute("notices", noticeListToShow);

            List<Integer> noticeIds = getMessageIds(noticeList);
            if (!noticeIds.isEmpty()) {
                messageService.checkMessage(noticeIds);
            }
        }
        return "/site/notice-detail";
    }
}
