package fi.aalto.forum.controller;

import fi.aalto.forum.service.ElasticsearchService;
import fi.aalto.forum.service.GiveLikeService;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.entity.DiscussPost;
import fi.aalto.forum.entity.Page;
import fi.aalto.forum.util.ForumConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements ForumConstant {

    private final ElasticsearchService elasticsearchService;
    private final UserService userService;
    private final GiveLikeService likeService;

    public SearchController(ElasticsearchService elasticsearchService, UserService userService, GiveLikeService likeService) {
        this.elasticsearchService = elasticsearchService;
        this.userService = userService;
        this.likeService = likeService;
    }

    /**
     * 用于搜索帖子，传递参数的方式为search?keyword=xxx&current=xxx，参数keyword和current会自动被获取
     */
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Model model, Page page) {
        /* 减1是因为页码是从1开始的 */
        int start = (page.getCurrent() - 1) * page.getLimit();
        Map<String, Object> tmpResult = elasticsearchService.searchDiscussPost(keyword, start, page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if (tmpResult.get("result") != null) {
            for (DiscussPost post : (List<DiscussPost>)tmpResult.get("result")) {
                Map<String, Object> map = new HashMap<>();
                // 存入帖子
                map.put("post", post);
                // 存入帖子作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 获取点赞的数量
                map.put("likeCount", likeService.findLikeNumberOfEntity(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 设置分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows((Integer) tmpResult.get("totalNumber"));
        return "/site/search";
    }

}
