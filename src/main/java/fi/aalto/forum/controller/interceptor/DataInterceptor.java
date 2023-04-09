package fi.aalto.forum.controller.interceptor;

import fi.aalto.forum.service.DataService;
import fi.aalto.forum.entity.User;
import fi.aalto.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercept the reuqests for calculating UV and DAU
 */
@Component
public class DataInterceptor implements HandlerInterceptor {

    private final DataService dataService;

    private final HostHolder hostHolder;

    public DataInterceptor(DataService dataService, HostHolder hostHolder) {
        this.dataService = dataService;
        this.hostHolder = hostHolder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /* Store the store ip for calculating UV(Unique Visitor) */
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        /* Store user id for calculating DAU(Daily Active User) */
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }

        return true;
    }

}
