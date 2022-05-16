package com.aalto.myBBS.controller.advice;

import com.aalto.myBBS.util.MybbsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class) // 设置只扫描带有Controller注解的Bean
public class ExceptionAdvice {

    // This component is used for recoding logs when error occurs
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * The method will handle the error occurs in the controller
     * @param e
     */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
        logger.error("Error occurs in the server" + e.getMessage());

        for(StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // For different request type, response different result
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = null;
            writer = response.getWriter();
            writer.write(MybbsUtil.getJSONString(500, "There is something wrong with the server"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
