package com.exchange.dto;

import java.time.LocalDateTime;

/**
 * 交换订单展示对象，附带双方物品和昵称，便于前端直接渲染。
 */
public class OrderVO {
    private Long orderId;
    private Long initiatorId;
    private String initiatorNickname;
    private Long targetId;
    private String targetNickname;
    private Long offerItemId;
    private String offerItemTitle;
    private String offerItemImage;
    private Long targetItemId;
    private String targetItemTitle;
    private String targetItemImage;
    private String remark;
    private Integer status;
    private Boolean initiatorConfirmed;
    private Boolean targetConfirmed;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
    public String getInitiatorNickname() { return initiatorNickname; }
    public void setInitiatorNickname(String initiatorNickname) { this.initiatorNickname = initiatorNickname; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTargetNickname() { return targetNickname; }
    public void setTargetNickname(String targetNickname) { this.targetNickname = targetNickname; }
    public Long getOfferItemId() { return offerItemId; }
    public void setOfferItemId(Long offerItemId) { this.offerItemId = offerItemId; }
    public String getOfferItemTitle() { return offerItemTitle; }
    public void setOfferItemTitle(String offerItemTitle) { this.offerItemTitle = offerItemTitle; }
    public String getTargetItemTitle() { return targetItemTitle; }
    public void setTargetItemTitle(String targetItemTitle) { this.targetItemTitle = targetItemTitle; }
    public Long getTargetItemId() { return targetItemId; }
    public void setTargetItemId(Long targetItemId) { this.targetItemId = targetItemId; }
    public String getOfferItemImage() { return offerItemImage; }
    public void setOfferItemImage(String offerItemImage) { this.offerItemImage = offerItemImage; }
    public String getTargetItemImage() { return targetItemImage; }
    public void setTargetItemImage(String targetItemImage) { this.targetItemImage = targetItemImage; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Boolean getInitiatorConfirmed() { return initiatorConfirmed; }
    public void setInitiatorConfirmed(Boolean initiatorConfirmed) { this.initiatorConfirmed = initiatorConfirmed; }
    public Boolean getTargetConfirmed() { return targetConfirmed; }
    public void setTargetConfirmed(Boolean targetConfirmed) { this.targetConfirmed = targetConfirmed; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getFinishTime() { return finishTime; }
    public void setFinishTime(LocalDateTime finishTime) { this.finishTime = finishTime; }
}
