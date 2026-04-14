package com.elderlycare.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结构
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码 ("0000"表示成功) */
    private String code;
    /** 消息 */
    private String message;
    /** 数据 */
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = "0000";
        result.message = "操作成功";
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = "0000";
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = "0000";
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.code = "0000";
        result.message = message;
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.code = "500";
        result.message = "服务器内部错误";
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = "500";
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.code = "400";
        result.message = message;
        return result;
    }

    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.code = "401";
        result.message = message;
        return result;
    }

    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<>();
        result.code = "403";
        result.message = message;
        return result;
    }

    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.code = "404";
        result.message = message;
        return result;
    }
}