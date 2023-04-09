package fi.aalto.forum.controller.interceptor;

import fi.aalto.forum.annotation.LoginRequired;
import fi.aalto.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Deprecated
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    private final HostHolder hostHolder;

    public LoginRequiredInterceptor(HostHolder hostHolder) {
        this.hostHolder = hostHolder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /* First judge whether the request that has been intercepted is a method, since it is also could be a request for static resource */
        if (handler instanceof HandlerMethod) {
            HandlerMethod  handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            /* Check whether the method has the LoginRequired annotation */
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            /* If the annotation is not null, meaning that this method requires login. Then check the content of the hostHolder method
               to check whether the user is login. */
            if (annotation != null && hostHolder.getUser() == null) {
                /* Redirect the user for login */
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}

