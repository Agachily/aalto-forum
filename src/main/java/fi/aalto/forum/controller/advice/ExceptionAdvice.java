package fi.aalto.forum.controller.advice;

import fi.aalto.forum.util.ForumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Scan only the classes annotated with the @Controller annotation
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

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

        /* For different request type, response different result */
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer = null;
            writer = response.getWriter();
            writer.write(ForumUtil.getJSONString(500, "There is something wrong with the server"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}
