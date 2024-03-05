package com.rachel.seckill.dao;

import com.rachel.seckill.domain.OrderInfo;
import com.rachel.seckill.domain.SeckillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {


    @Select("select * from seckill_order where user_id = #{userId} and goods_id = #{goodsId}")
    public SeckillOrder getSeckillOrderByUserIdGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

    @Insert("insert into seckill_order (user_id, order_id, goods_id) values (#{userId}, #{orderId}, #{goodsId})")
    public int insertSeckillOrder(SeckillOrder seckillOrder);

    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
            + "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    public long insertOrderInfo(OrderInfo orderInfo);

    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId") long orderId);


    @Delete("delete from order_info")
    public void deleteOrders();

    @Delete("delete from seckill_order")
    public void deleteSeckillOrders();
}
