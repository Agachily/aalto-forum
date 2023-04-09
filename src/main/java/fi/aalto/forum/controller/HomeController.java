package fi.aalto.forum.controller;

import fi.aalto.forum.service.GiveLikeService;
import fi.aalto.forum.entity.DiscussPost;
import fi.aalto.forum.entity.Page;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.service.DiscussPostService;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.util.ForumConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements ForumConstant {

    private final DiscussPostService discussPostService;
    private final UserService userService;
    private final GiveLikeService giveLikeService;

    public HomeController(DiscussPostService discussPostService, UserService userService, GiveLikeService giveLikeService) {
        this.discussPostService = discussPostService;
        this.userService = userService;
        this.giveLikeService = giveLikeService;
    }

    /**
     * 方法调用前, SpringMVC会自动实例化Model和Page, 并将Page注入Model. 所以, 在thymeleaf中可以直接访问Page对象中的数据.
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null) {
            for(DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                /* Store the post data and the corresponding user into the map object */
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                map.put("post", post);
                /* Check how many "like" has been given to that post */
                map.put("likeNumber", giveLikeService.findLikeNumberOfEntity(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        model.addAttribute("page", page);
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
