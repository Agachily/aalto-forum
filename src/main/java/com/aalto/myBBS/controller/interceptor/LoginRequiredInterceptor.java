package com.aalto.myBBS.controller.interceptor;

import com.aalto.myBBS.annotation.LoginRequired;
import com.aalto.myBBS.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // First judge whether the request that has been intercepted is a method, for it is also could be a request for static resource
        if (handler instanceof HandlerMethod) {
            HandlerMethod  handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            // If the annotation is not null, meaning that this method requires login. Then check the content of the hostHolder method
            // to check whether the user is login.
            if (annotation != null && hostHolder.getUser() == null) {
                // Redirect the user for login
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}

