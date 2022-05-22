package com.aalto.myBBS.controller;

import com.aalto.myBBS.service.GiveLikeService;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.util.HostHolder;
import com.aalto.myBBS.util.MybbsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class GiveLikeController {
    @Autowired
    private GiveLikeService giveLikeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "like", method = RequestMethod.POST)
    @ResponseBody
    public String giveOrCancelLike(int entityType, int entityId, int entityUserId) {
        // Get the current user
        User user = hostHolder.getUser();
        giveLikeService.giveOrCancelLike(user.getId(), entityType, entityId, entityUserId);

        long likeNumberOfEntity = giveLikeService.findLikeNumberOfEntity(entityType, entityId);
        int status = giveLikeService.checkLikeStatusOfEntity(user.getId(), entityType, entityId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount", likeNumberOfEntity);
        map.put("likeStatus", status);

        return MybbsUtil.getJSONString(200, null, map);
    }
}
