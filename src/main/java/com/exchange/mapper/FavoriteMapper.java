package com.exchange.mapper;

import com.exchange.entity.Item;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    // 关注物品，使用 INSERT IGNORE 避免重复关注报错。
    @Insert("INSERT IGNORE INTO user_favorite(user_id, item_id, create_time) VALUES(#{userId}, #{itemId}, NOW())")
    int insert(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // 取消关注。
    @Delete("DELETE FROM user_favorite WHERE user_id = #{userId} AND item_id = #{itemId}")
    int delete(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // 我的关注列表，联动物品状态给前端展示。
    @Select("SELECT i.* FROM user_favorite f JOIN busi_item i ON f.item_id = i.item_id WHERE f.user_id = #{userId} ORDER BY f.create_time DESC")
    List<Item> selectFavoriteItems(Long userId);
}
