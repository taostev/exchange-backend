package com.exchange.mapper;

import com.exchange.entity.Item;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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

    // 根据物品 ID 查询详情，详情页和订单校验都会使用。
    Item selectById(Long itemId);

    // 查询某个用户发布的全部物品，发起交换时用于选择自己的在架物品。
    @Select("SELECT * FROM busi_item WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<Item> selectByUserId(Long userId);

    @Select("SELECT * FROM busi_item WHERE user_id = #{userId} AND status = #{status} ORDER BY update_time DESC")
    List<Item> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM busi_item WHERE status = 1 AND user_id <> #{userId} AND category_id = #{categoryId} ORDER BY update_time DESC LIMIT #{limit}")
    List<Item> selectRecommendByCategory(@Param("categoryId") Integer categoryId,
                                         @Param("userId") Long userId,
                                         @Param("limit") int limit);

    @Select("SELECT * FROM busi_item WHERE status = 1 AND user_id <> #{userId} ORDER BY update_time DESC LIMIT #{limit}")
    List<Item> selectRecommendFallback(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM busi_item WHERE status = 1 AND user_id <> #{userId} ORDER BY update_time DESC LIMIT #{limit}")
    List<Item> selectRecommendPool(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("<script>"
            + "SELECT * FROM busi_item WHERE status = 1 AND item_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<Item> selectAvailableByIds(@Param("ids") List<Long> ids);

    // 普通上下架或管理员强制下架都通过状态字段控制展示。
    @Update("UPDATE busi_item SET status = #{status}, update_time = NOW() WHERE item_id = #{itemId}")
    int updateStatus(@Param("itemId") Long itemId, @Param("status") Integer status);

    // 乐观锁：只有在架物品才能被锁定为交换中，防止多人同时抢同一件物品。
    @Update("UPDATE busi_item SET status = 2, update_time = NOW() WHERE item_id = #{itemId} AND status = 1")
    int lockAvailableItem(Long itemId);

    // 订单完成或取消时批量恢复/结项物品状态。
    @Update("UPDATE busi_item SET status = #{status}, update_time = NOW() WHERE item_id = #{firstItemId} OR item_id = #{secondItemId}")
    int updateTwoItemsStatus(@Param("firstItemId") Long firstItemId,
                             @Param("secondItemId") Long secondItemId,
                             @Param("status") Integer status);

    // 后台统计当前在架物品数量。
    @Select("SELECT COUNT(*) FROM busi_item WHERE status = 1")
    int countAvailableItems();
}