package com.leben.drinkshop.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 统一 API响应格式
 * 格式: { "success": true, "code": 200, "message": "...", "data": ... }
 */
@Setter
@Getter
public class CommonEntity<T> {
    private boolean success; // 替换原来的 state
    private int code;        // 新增状态码
    private String message;
    private T data;
    private long timestamp;

    public CommonEntity() {
        this.timestamp = System.currentTimeMillis();
    }

    public CommonEntity(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> CommonEntity<T> success(T data) {
        return new CommonEntity<>(true, 200, "操作成功", data);
    }

    public static <T> CommonEntity<T> success(String message, T data) {
        return new CommonEntity<>(true, 200, message, data);
    }

    public static <T> CommonEntity<T> error(String message) {
        return new CommonEntity<>(false, 500, message, null);
    }

    public static <T> CommonEntity<T> error(int code, String message) {
        return new CommonEntity<>(false, code, message, null);
    }

}