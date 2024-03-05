package com.rachel.seckill.service;

import com.rachel.seckill.dao.GoodsDao;
import com.rachel.seckill.domain.SeckillGoods;
import com.rachel.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo() {
       return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsById(long goodsId) {
        return goodsDao.getGoodsById(goodsId);
    }

    public boolean reduceStock(GoodsVo goods) {
        SeckillGoods sg = new SeckillGoods();
        sg.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(sg);
        return ret > 0;
    }

    public void resetStock(List<GoodsVo> goodsList) {
        for(GoodsVo goods : goodsList ) {
            SeckillGoods g = new SeckillGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsDao.resetStock(g);
        }
    }
}
