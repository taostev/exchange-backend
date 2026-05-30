package com.exchange.entity;

import java.time.LocalDateTime;

public class Item {
    private Long itemId;          // 物品唯一主键
    private Long userId;          // 发布者ID
    private Integer categoryId;   // 物品所属的二级分类ID
    private String title;         // 物品标题
    private String description;   // 物品详细描述
    private String exchangeWish;  // 期望换到的物品说明
    private String images;        // 物品图片URL路径
    private Integer status;       // 当前状态：1-在架，2-交换中，3-已换出，4-下架
    private LocalDateTime updateTime; // 信息最后更新时间

    // 无参构造方法
    public Item() {}

    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExchangeWish() { return exchangeWish; }
    public void setExchangeWish(String exchangeWish) { this.exchangeWish = exchangeWish; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}