package com.rachel.seckill.dao;

import com.rachel.seckill.domain.SeckillGoods;
import com.rachel.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select g.*, sg.seckill_price, sg.stock_count, sg.start_date, sg.end_date from goods g left join seckill_goods sg on g.id = sg.goods_id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*, sg.seckill_price, sg.stock_count, sg.start_date, sg.end_date from goods g left join seckill_goods sg on g.id = sg.goods_id where g.id = #{goodsId}")
    public GoodsVo getGoodsById(@Param("goodsId") long goodsId);

    /**
     * MyBatis的update操作会返回更新影响的行数。如果需要获取这个返回值，可以使用int类型来接收结果。
     * @param sg
     * @return
     */
    @Update("update seckill_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    public int reduceStock(SeckillGoods sg);

    @Update("update seckill_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
    public int resetStock(SeckillGoods g);

}
