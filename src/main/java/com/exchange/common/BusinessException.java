package com.exchange.common;

/**
 * 业务异常：用于主动中断不符合业务规则的操作，例如物品已被锁定、无权限操作等。
 */
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
