package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.utils.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultUtil.Result<Void> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResultUtil.error(400, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResultUtil.Result<Void> handleIllegalStateException(IllegalStateException exception) {
        return ResultUtil.error(409, exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResultUtil.Result<Void> handleAccessDeniedException(AccessDeniedException exception) {
        return ResultUtil.error(403, "无权限访问");
    }

    @ExceptionHandler(Exception.class)
    public ResultUtil.Result<Void> handleException(Exception exception) {
        log.error("未捕获的异常", exception);
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "服务器内部错误";
        }
        return ResultUtil.error(500, message);
    }
}
