package com.exchange.dto;

/**
 * 发起交换请求体：选择自己的物品 offerItemId 去交换对方的 targetItemId。
 */
public class CreateOrderRequest {
    private Long offerItemId;
    private Long targetItemId;
    private String remark;

    public Long getOfferItemId() { return offerItemId; }
    public void setOfferItemId(Long offerItemId) { this.offerItemId = offerItemId; }
    public Long getTargetItemId() { return targetItemId; }
    public void setTargetItemId(Long targetItemId) { this.targetItemId = targetItemId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
