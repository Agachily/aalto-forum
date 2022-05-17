package com.aalto.myBBS.controller;

import com.aalto.myBBS.annotation.LoginRequired;
import com.aalto.myBBS.service.MessageService;
import com.aalto.myBBS.service.UserService;
import com.aalto.myBBS.service.entity.Message;
import com.aalto.myBBS.service.entity.Page;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.util.HostHolder;
import com.aalto.myBBS.util.MybbsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /** hostHolder is used to get th information of current logged user */
    @Autowired
    private HostHolder hostHolder;

    /**
     * Get the letter list for a certain user
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    @LoginRequired
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息，依旧是在Page对象中设置分页信息
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
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 获取与用户进行通信的User对象
                map.put("target", getLetterTarget(message.getConversationId()));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    /**
     * 显示私信会话的详细信息
     * @param conversationId
     * @param page
     * @param model
     * @return
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
            return MybbsUtil.getJSONString(400, "The receiver does not exist");
        }
        if (receiver.getId() == currentUser.getId()) {
            System.out.println("sada" + content + "asdas");
            return MybbsUtil.getJSONString(400, "You should not send letter to yourself");
        }
        if (content.equals("")) {
            return MybbsUtil.getJSONString(400, "The content is empty");
        }

        // 构造Message对象
        Message message = new Message();
        message.setFromId(currentUser.getId());
        message.setToId(receiver.getId());
        // 拼接会话Id，要求Id小的在前
        if (message.getFromId() < message.getToId())
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        else
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);

        return MybbsUtil.getJSONString(200, "The message has been sent");
    }

    /**
     * 获取与当前用户进行对话的用户
     * @param conversationId
     * @return
     */
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
}
