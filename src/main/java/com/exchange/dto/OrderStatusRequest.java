package com.exchange.dto;

/**
 * 订单状态操作请求：ACCEPT/REJECT/CANCEL/FINISH 分别表示同意、拒绝、取消、完成。
 */
public class OrderStatusRequest {
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
