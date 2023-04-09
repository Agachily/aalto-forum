package fi.aalto.forum.controller.interceptor;

import fi.aalto.forum.entity.LoginTicket;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.service.UserService;
import fi.aalto.forum.util.CookieUtil;
import fi.aalto.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    private final UserService userService;

    private final HostHolder hostHolder;

    public LoginTicketInterceptor(UserService userService, HostHolder hostHolder) {
        this.userService = userService;
        this.hostHolder = hostHolder;
    }

    /**
     * The preHandle is executed before the controller. Before executing the handler, get the user info and store it.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /* Get the ticket from the cookie */
        String ticket = CookieUtil.getValue(request, "ticket");
        /* Get the user information according to the ticket */
        if (ticket != null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            /* Judge whether the obtained ticket is valid */
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                /* If the ticket is valid, get the user according to the userId stored in ticket */
                User user = userService.findUserById(loginTicket.getUserId());
                /* Put the obtained user object to the HostHolder */
                hostHolder.setUser(user);
                /* Build the structure of user authentication and store it in SecurityContext so that Spring Security can use it */
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        /* Check the authorities that current user has */
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    /**
     * This is executed after the executing the controller before the executing of template engine.
     * Put the obtained user object to the ModelAndView object for the template engine to display the user info
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    /**
     * Clear the data after the request is processed
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SecurityContextHolder.clearContext();
        hostHolder.clear();
    }
}
