package com.leben.drinkshop.handler;

import com.leben.drinkshop.dto.CommonEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 捕获所有 Controller 层抛出的异常，统一返回 ApiResponse
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonEntity<String> handleException(Exception e) {
        // 打印错误堆栈到控制台，方便后端排查 (生产环境建议用 log.error)
        e.printStackTrace();

        // 这里的 e.getMessage() 就是你在 Service 里 throw new RuntimeException("账号已存在") 的那句话
        return CommonEntity.error(500, e.getMessage());
    }
}