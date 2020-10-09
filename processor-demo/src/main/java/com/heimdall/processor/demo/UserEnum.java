package com.heimdall.processor.demo;


import com.heimdall.processor.core.MappingConstant;

@MappingConstant(withEnumName = true)
public enum  UserEnum implements IErrorCode {

    /**
     * 错误码
     */
    PASSWORD_ERROR(1002, "密码错误, 用户名: {0}"),

    USERNAME_ERROR(1001, "用户名错误, 用户名: {0}"),

    USER_NOT_EXIST(1000, "用户不存在, 用户名: {0}");

    private final int code;

    private final String msg;


    UserEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
