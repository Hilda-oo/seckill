package com.rachel.seckill.controller;

import com.rachel.seckill.domain.OrderInfo;
import com.rachel.seckill.domain.SeckillUser;
import com.rachel.seckill.result.CodeMsg;
import com.rachel.seckill.result.Result;
import com.rachel.seckill.service.GoodsService;
import com.rachel.seckill.service.OrderService;
import com.rachel.seckill.vo.GoodsVo;
import com.rachel.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping(value = "/detail/{orderId}")
    @ResponseBody
    public Result<OrderDetailVo> detail(HttpServletRequest request, HttpServletResponse response, SeckillUser seckillUser, @PathVariable("orderId") long orderId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = orderInfo.getGoodsId();
        GoodsVo goods = goodsService.getGoodsById(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrder(orderInfo);
        orderDetailVo.setGoods(goods);

        return Result.success(orderDetailVo);
    }
}
