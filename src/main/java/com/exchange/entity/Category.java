package com.exchange.entity;

/**
 * 分类字典实体：管理员维护物品大类/小类，物品通过 categoryId 关联。
 */
public class Category {
    private Long categoryId;
    private Long parentId;
    private String name;
    private Integer sort;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
