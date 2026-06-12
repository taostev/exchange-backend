package com.exchange.mapper;

import com.exchange.entity.ExchangeOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExchangeOrderMapper {

    // 新增交换意向订单，初始状态为 0-待确认。
    @Insert("INSERT INTO busi_exchange_order(initiator_id, target_id, offer_item_id, target_item_id, remark, status, create_time) " +
            "VALUES(#{initiatorId}, #{targetId}, #{offerItemId}, #{targetItemId}, #{remark}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insert(ExchangeOrder order);

    // 根据订单 ID 查询订单详情，状态流转前必须先读取并校验权限。
    @Select("SELECT * FROM busi_exchange_order WHERE order_id = #{orderId}")
    ExchangeOrder selectById(Long orderId);

    // 查询与当前用户相关的订单，覆盖“我发起的”和“我收到的”两类清单。
    @Select("SELECT * FROM busi_exchange_order WHERE initiator_id = #{userId} OR target_id = #{userId} ORDER BY create_time DESC")
    List<ExchangeOrder> selectByUserId(Long userId);

    // 管理员查看全站订单流水。
    @Select("SELECT * FROM busi_exchange_order ORDER BY create_time DESC")
    List<ExchangeOrder> selectAll();

    // 更新订单状态，完成/取消时写 finish_time。
    @Update("UPDATE busi_exchange_order SET status = #{status}, finish_time = #{finishTime} WHERE order_id = #{orderId}")
    int updateStatus(ExchangeOrder order);

    // 后台统计进行中的订单。
    @Select("SELECT COUNT(*) FROM busi_exchange_order WHERE status = 1")
    int countProcessingOrders();
}
