package com.exchange.dto;

/**
 * 物品发布者对外展示信息，按权限决定是否返回联系方式。
 */
public class PublisherVO {
    private Long userId;
    private String nickname;
    private String profile;
    private String contactInfo;
    private boolean contactVisible;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public boolean isContactVisible() { return contactVisible; }
    public void setContactVisible(boolean contactVisible) { this.contactVisible = contactVisible; }
}
