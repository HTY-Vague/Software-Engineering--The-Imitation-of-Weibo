package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.utils.ResultUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        return ResultUtil.error(exception.getMessage());
    }
}
