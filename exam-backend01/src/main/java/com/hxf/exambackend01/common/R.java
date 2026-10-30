package com.hxf.exambackend01.common;

public class R<T> {
    private Integer code;      // 200 成功
    private String message;    // 提示信息
    private T data;            // 数据

    // 构造方法
    public R() {}

    // getter / setter(Jackson 序列化必须的)
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    // 成功
    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    // 失败(修正逻辑 bug,使用传入的 code)
    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }

    // 失败(单参数版本,供 GlobalExceptionHandler 用)
    public static <T> R<T> error(String message) {
        return error(500, message);
    }
}