package com.test.guo.common.result;

import com.test.guo.common.exception.ErrorCode;

//所有接口统一返回的数据结构,@param <T> data 中保存的数据类型

public class Result<T> {
   private Integer code;
   private String message;
   private T data;

   public Result(){

   }

    public Result(Integer code,String message,T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
       return new Result<>(200, "操作成功", data);
    }

    public static Result<Void> success() {
       return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> error(Integer code, String message) {
       return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
