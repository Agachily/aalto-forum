package fi.aalto.forum.config;

import fi.aalto.forum.util.ForumConstant;
import fi.aalto.forum.util.ForumUtil;
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
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ForumConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
        /* Don't intercept the access to static resources */
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow")
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful")
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                ) // 设置只有版主才能进行置顶和加精操作
                .antMatchers(
                        "/discuss/delete",
                        "/data/**")
                .hasAnyAuthority(
                        /* Only user with admin authority can delete post */
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                /* Disable CSRF */
                .and().csrf().disable();

        /* accessDeniedHandler()配置登陆但权限不足时如何处理，authenticationEntryPoint()配置没有登陆时如何处理 */
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        /* Return JSON string if it is async request */
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(ForumUtil.getJSONString(403, "Login Required"));
                        } else {
                            /* Redirect to the "login" page if it is normal request */
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(ForumUtil.getJSONString(403, "No Authorization"));
                        } else {
                            /* Redirect to the "denied" page if it is normal request */
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        /*Spring Security底层默认会拦截/logout请求，进行退出处理。只有覆盖它默认的逻辑，让它去拦截别的路径，才能执行我们自己的逻辑*/
        http.logout().logoutUrl("/fakeLogout");
    }

}
