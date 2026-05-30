package com.exchange.mapper;

import com.exchange.entity.Item;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ItemMapper {

    // 插入物品数据
    @Insert("INSERT INTO busi_item(user_id, category_id, title, description, exchange_wish, images, status, update_time) " +
            "VALUES(#{userId}, #{categoryId}, #{title}, #{description}, #{exchangeWish}, #{images}, #{status}, NOW())")
    // 自动获取自增的主键 id 并赋值给 Item 对象的 itemId 属性
    @Options(useGeneratedKeys = true, keyProperty = "itemId")
    int insertItem(Item item);

    /**
     * 核心：动态条件分页查询物品列表
     * @param offset    跳过的行数（(page-1)*size）
     * @param size      每页查询的数量
     * @param categoryId 分类ID（可选）
     * @param keyword    关键词（可选）
     */
    List<Item> selectItemPage(@Param("offset") int offset,
                              @Param("size") int size,
                              @Param("categoryId") Integer categoryId,
                              @Param("keyword") String keyword);

    /**
     * 核心：获取符合当前条件的总条数（用于前端计算总页数）
     */
    int selectItemCount(@Param("categoryId") Integer categoryId,
                        @Param("keyword") String keyword);
}