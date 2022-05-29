package com.aalto.myBBS.config;

import com.aalto.myBBS.util.MybbsConstant;
import com.aalto.myBBS.util.MybbsUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class is use for configuring Spring Security
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements MybbsConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
        /* 忽略对静态资源的拦截 */
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * 授权，antMatchers中配置要拦截的路径，hasAnyAuthority配置那些觉得才可以访问这些路径
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow")
                .hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR)
                .anyRequest().permitAll() // 除了以上那些路径之外，其余任何请求都允许
                .and().csrf().disable(); // 禁用csrf

        /* accessDeniedHandler()配置登陆但权限不足时如何处理，authenticationEntryPoint()配置没有登陆时如何处理 */
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        /* 如果是异步请求，则返回JSON字符串 */
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(MybbsUtil.getJSONString(403, "Login Required"));
                        } else {
                            /* 如果是普通请求，就重定向到登陆页面 */
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        /* 如果是异步请求，则返回JSON字符串 */
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(MybbsUtil.getJSONString(403, "No Authorization"));
                        } else {
                            /* 如果是普通请求，就重定向到权限不足页面 */
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        /*Spring Security底层默认会拦截/logout请求，进行退出处理。只有覆盖它默认的逻辑，让它去拦截别的路径，才能执行我们自己的逻辑*/
        http.logout().logoutUrl("/fakeLogout");
    }

}
