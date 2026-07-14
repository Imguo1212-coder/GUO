package com.test.guo.common.exception;

public enum ErrorCode {    //enum = 枚举。错误类型是固定的几种，不能随便 invent 一个字符串当错误码，避免写错、写乱。

    VALIDATION_ERROR(40001, "请求参数不合法", 400),
    USER_NOT_FOUND(40401, "用户不存在", 404),
    DEPARTMENT_NOT_FOUND(40402, "部门不存在", 404),
    USER_OPERATION_FAILED(50001, "用户操作失败", 500),
    DEPARTMENT_OPERATION_FAILED(50002, "部门操作失败", 500),
    INTERNAL_ERROR(50000, "服务器内部错误", 500);

    private final int code;      //final：创建后不能改，错误定义是常量。
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public int getHttpStatus() { return httpStatus; }
}