package com.rachel.seckill.controller;

import com.rachel.seckill.domain.SeckillOrder;
import com.rachel.seckill.domain.SeckillUser;
import com.rachel.seckill.rabbitmq.MQSender;
import com.rachel.seckill.rabbitmq.SeckillMessage;
import com.rachel.seckill.redis.GoodsKey;
import com.rachel.seckill.redis.OrderKey;
import com.rachel.seckill.redis.RedisService;
import com.rachel.seckill.redis.SeckillGoodsKey;
import com.rachel.seckill.result.CodeMsg;
import com.rachel.seckill.result.Result;
import com.rachel.seckill.service.GoodsService;
import com.rachel.seckill.service.OrderService;
import com.rachel.seckill.service.SeckillService;
import com.rachel.seckill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goods = goodsService.listGoodsVo();
        for (GoodsVo good: goods) {
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + good.getId(), good.getStockCount());
            localOverMap.put(good.getId(), false);
        }
    }

    /**
     * qps:2628
     * 5000*10
     * 卖超，剩余库存<0
     * 打包命令：mvn clean package
     * 测试命令： sh jmeter.sh -n -t ../../../code/java/work/jmeter/seckill.jmx -l ../../../code/java/work/jmeter/result.jtl
     * 优化后：
     * qps:5928
     * 5000*10
     * @param model
     * @param seckillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> seckill(SeckillUser seckillUser, Model model, @RequestParam("goodsId") long goodsId, @PathVariable("path") String path) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = seckillService.checkSeckillPath(seckillUser, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //减少redis缓存访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        //判断重复秒杀
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(seckillUser.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //入队
        SeckillMessage message = new SeckillMessage();
        message.setUser(seckillUser);
        message.setGoodId(goodsId);
        sender.sendSeckillMessage(message);
        return Result.success(0);
//        //判断库存
//        GoodsVo goods = goodsService.getGoodsById(goodsId);
//        int stockCount = goods.getStockCount();
//        if (stockCount <= 0) {
//            return Result.error(CodeMsg.SECKILL_OVER);
//        }
//        //判断重复秒杀
//        SeckillOrder seckillOrder = orderService.getSeckillOrderByUserIdGoodsId(seckillUser.getId(), goodsId);
//        if (seckillOrder != null) {
//            return Result.error(CodeMsg.REPEATE_SECKILL);
//        }
//        //秒杀
//        OrderInfo orderInfo = seckillService.seckill(seckillUser, goods);
//        return Result.success(orderInfo);
    }

    @RequestMapping(value = "/path/{goodsId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(SeckillUser seckillUser, Model model, @PathVariable("goodsId") long goodsId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        String path = seckillService.createSeckillPath(seckillUser, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(SeckillUser seckillUser, HttpServletResponse response, @RequestParam("goodsId")long goodsId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = seckillService.createVerifyCode(seckillUser, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }

    /**
     * orderId: 秒杀成功
     * 0：排队中
     * -1： 秒杀失败
     * @param seckillUser
     * @param model
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result/{goodsId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> result(SeckillUser seckillUser, Model model, @PathVariable("goodsId") long goodsId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = seckillService.getSeckillResult(seckillUser.getId(), goodsId);
        return Result.success(result);
    }

    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getSeckillGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getSeckillOrderByUidGid);
        redisService.delete(SeckillGoodsKey.isGoodsOver);
        seckillService.reset(goodsList);
        return Result.success(true);
    }


}
