package com.aalto.myBBS.controller.interceptor;

import com.aalto.myBBS.service.entity.LoginTicket;
import com.aalto.myBBS.service.entity.User;
import com.aalto.myBBS.service.UserService;
import com.aalto.myBBS.util.CookieUtil;
import com.aalto.myBBS.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the ticket from the cookie
        String ticket = CookieUtil.getValue(request, "ticket");
        // Get the user information according to the ticket
        if (ticket != null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // Judge whether the obtained ticket is valid
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // If the ticket is valid, get the user according to the ticket
                User user = userService.findUserById(loginTicket.getUserId());
                // Put the obtained user object to the HttpRequest
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    /**
     * Put the obtained user object to the ModelAndView object
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    /**
     * Clear the data after the final call
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
