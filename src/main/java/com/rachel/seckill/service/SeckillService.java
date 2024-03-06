package com.rachel.seckill.service;

import com.rachel.seckill.domain.OrderInfo;
import com.rachel.seckill.domain.SeckillOrder;
import com.rachel.seckill.domain.SeckillUser;
import com.rachel.seckill.redis.RedisService;
import com.rachel.seckill.redis.SeckillGoodsKey;
import com.rachel.seckill.util.MD5Util;
import com.rachel.seckill.util.UUIDUtil;
import com.rachel.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class SeckillService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    public BufferedImage createVerifyCode(SeckillUser seckillUser, long goodsId) {
        if(seckillUser == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(SeckillGoodsKey.getSeckillVerifyCode, seckillUser.getId()+"_"+goodsId, rnd);
        //输出图片
        return image;
    }

    public boolean checkVerifyCode(SeckillUser seckillUser, long goodsId, int verifyCode) {
        if(seckillUser == null || goodsId <=0) {
            return false;
        }
        Integer value = redisService.get(SeckillGoodsKey.getSeckillVerifyCode, seckillUser.getId() + "_" + goodsId, Integer.class);
        if (value == null || value - verifyCode != 0) {
            return false;
        }
        redisService.delete(SeckillGoodsKey.getSeckillVerifyCode, seckillUser.getId() + "_" + goodsId);
        return true;
    }

    private int calc(String exp) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        try {
            return (Integer)engine.eval(exp);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] opts = new char[]{'+', '-', '*'};
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char opt1 = opts[rdm.nextInt(3)];
        char opt2 = opts[rdm.nextInt(3)];
        String exp = "" + num1 + opt1 + num2 + opt2 + num3;
        return exp;
    }

    @Transactional
    public OrderInfo seckill(SeckillUser seckillUser, GoodsVo goods) {
        //减库存
        boolean success = goodsService.reduceStock(goods);
        if (success) {
            //下订单 写入秒杀订单
            return orderService.createOrder(seckillUser, goods);
        } else {
            setSeckillOver(goods.getId());
            return null;
        }
    }

    public long getSeckillResult(long userId, long goodsId) {
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            return order.getOrderId();
        } else {
            boolean ret = getSeckillOver(goodsId);
            if (ret) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private void setSeckillOver(long goodsId) {
        redisService.set(SeckillGoodsKey.isGoodsOver, "" + goodsId, true);
    }
    private boolean getSeckillOver(long goodsId) {
        return redisService.exist(SeckillGoodsKey.isGoodsOver, "" +goodsId);
    }

    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    public String createSeckillPath(SeckillUser seckillUser, long goodsId) {
        if (seckillUser == null || goodsId <= 0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(SeckillGoodsKey.getSeckillPath, "" + seckillUser.getId() + "_" + goodsId, str);
        return str;
    }

    public boolean checkSeckillPath(SeckillUser seckillUser, long goodsId, String path) {
        if (seckillUser == null || path == null || goodsId <= 0) {
            return false;
        }
        String val = redisService.get(SeckillGoodsKey.getSeckillPath, "" + seckillUser.getId() + "_" + goodsId, String.class);
        return val.equals(path);
    }


}
