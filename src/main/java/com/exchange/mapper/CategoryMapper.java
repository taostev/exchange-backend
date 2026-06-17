package com.exchange.mapper;

import com.exchange.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    // 分类树列表，前台筛选和后台维护共用。
    @Select("SELECT * FROM sys_category ORDER BY parent_id ASC, sort ASC, category_id ASC")
    List<Category> selectAll();

    // 新增分类。
    @Insert("INSERT INTO sys_category(parent_id, name, sort) VALUES(#{parentId}, #{name}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "categoryId")
    int insert(Category category);

    // 修改分类名称、父级和排序。
    @Update("UPDATE sys_category SET parent_id = #{parentId}, name = #{name}, sort = #{sort} WHERE category_id = #{categoryId}")
    int update(Category category);

    // 删除分类前由 Service/Controller 校验该分类下是否挂载物品。
    @Delete("DELETE FROM sys_category WHERE category_id = #{categoryId}")
    int delete(Long categoryId);

    // 校验分类下是否存在物品，存在则拒绝删除。
    @Select("SELECT COUNT(*) FROM busi_item WHERE category_id = #{categoryId}")
    int countItems(Long categoryId);

    @Select("SELECT * FROM sys_category WHERE name = #{name} LIMIT 1")
    Category findByName(String name);

    @Update("UPDATE busi_item SET category_id = #{targetCategoryId}, update_time = NOW() WHERE category_id = #{sourceCategoryId}")
    int transferItems(@Param("sourceCategoryId") Long sourceCategoryId, @Param("targetCategoryId") Long targetCategoryId);
}
