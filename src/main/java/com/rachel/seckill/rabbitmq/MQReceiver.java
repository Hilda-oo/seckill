package com.rachel.seckill.rabbitmq;

import com.rachel.seckill.domain.OrderInfo;
import com.rachel.seckill.domain.SeckillOrder;
import com.rachel.seckill.domain.SeckillUser;
import com.rachel.seckill.redis.RedisService;
import com.rachel.seckill.redis.SeckillGoodsKey;
import com.rachel.seckill.result.CodeMsg;
import com.rachel.seckill.result.Result;
import com.rachel.seckill.service.GoodsService;
import com.rachel.seckill.service.OrderService;
import com.rachel.seckill.service.SeckillService;
import com.rachel.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void receiveSeckillMessage(String message) {
        SeckillMessage seckillMessage = RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser user = seckillMessage.getUser();
        long goodId = seckillMessage.getGoodId();
        //判断库存
        GoodsVo goods = goodsService.getGoodsById(goodId);
        int stockCount = goods.getStockCount();
        if (stockCount <= 0) {
            return ;
        }
        //判断重复秒杀
        SeckillOrder seckillOrder = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodId);
        if (seckillOrder != null) {
            return ;
        }
        //秒杀
        seckillService.seckill(user, goods);
    }

//    @RabbitListener(queues = MQConfig.QUEUE)
//    public void receive(String message) {
//        logger.info("receive:" +message);
//    }
//
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//    public void receiveTopic1(String message) {
//        logger.info("topic queue1 receive:" +message);
//    }
//
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//    public void receiveTopic2(String message) {
//        logger.info("topic queue2 receive:" +message);
//    }
//
//    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
//    public void receiveHeaders(byte[] message) {
//        logger.info("header queue receive:" + new String(message));
//    }

}
