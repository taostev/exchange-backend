package com.exchange.entity;

import java.time.LocalDateTime;

/**
 * 交换订单实体：记录以物易物从发起、同意/拒绝到完成/取消的生命周期。
 */
public class ExchangeOrder {
    private Long orderId;
    private Long initiatorId;
    private Long targetId;
    private Long offerItemId;
    private Long targetItemId;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getOfferItemId() { return offerItemId; }
    public void setOfferItemId(Long offerItemId) { this.offerItemId = offerItemId; }
    public Long getTargetItemId() { return targetItemId; }
    public void setTargetItemId(Long targetItemId) { this.targetItemId = targetItemId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getFinishTime() { return finishTime; }
    public void setFinishTime(LocalDateTime finishTime) { this.finishTime = finishTime; }
}
