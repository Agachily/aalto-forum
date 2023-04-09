package fi.aalto.forum.config;

import fi.aalto.forum.controller.interceptor.DataInterceptor;
import fi.aalto.forum.controller.interceptor.LoginTicketInterceptor;
import fi.aalto.forum.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private LoginTicketInterceptor loginTicketInterceptor;

    /* 重构项目，使用Spring Security来进行权限管理，因此将之前的拦截器废弃 */
    // @Autowired
    // private LoginRequiredInterceptor loginRequiredInterceptor;

    private MessageInterceptor messageInterceptor;

    private DataInterceptor dataInterceptor;

    public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor, MessageInterceptor messageInterceptor, DataInterceptor dataInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
        this.messageInterceptor = messageInterceptor;
        this.dataInterceptor = dataInterceptor;
    }

    /**
     * Interceptors configuration
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /* Omit the configuration of addPathPatterns here because these interceptors are used for all paths */
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg", "/**/*.png");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg", "/**/*.png");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }

}
