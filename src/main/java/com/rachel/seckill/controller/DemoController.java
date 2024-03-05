package com.rachel.seckill.controller;

import com.rachel.seckill.domain.User;
import com.rachel.seckill.rabbitmq.MQSender;
import com.rachel.seckill.redis.RedisService;
import com.rachel.seckill.redis.UserKey;
import com.rachel.seckill.result.CodeMsg;
import com.rachel.seckill.result.Result;
import com.rachel.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "hello world!";
    }

    //1. rest api json输出
        //json:{ code  msg   data}
    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello, rachel");
//        return new Result(0, "success", "hello, rachel");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
//        return new Result(500102, "error");
    }
//
//    @RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mq() {
//        String msg = "hello,rachel";
//        mqSender.send(msg);
//        return Result.success(msg);
//    }
//
//    @RequestMapping("/mq/topic")
//    @ResponseBody
//    public Result<String> mqTopic() {
//        String msg = "hello,rachel";
//        mqSender.sendTopic(msg);
//        return Result.success(msg);
////        return new Result(500102, "error");
//    }
//
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public Result<String> mqFanout() {
//        String msg = "hello,rachel";
//        mqSender.sendFanout(msg);
//        return Result.success(msg);
////        return new Result(500102, "error");
//    }
//
//    @RequestMapping("/mq/headers")
//    @ResponseBody
//    public Result<String> mqHeaders() {
//        String msg = "hello,rachel";
//        mqSender.sendHeader(msg);
//        return Result.success(msg);
////        return new Result(500102, "error");
//    }

    //2. 页面
    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "rachel");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User();
        user.setName("1111");
        user.setId(1);
        boolean ret = redisService.set(UserKey.getById, "" + 1, user);
        return Result.success(ret);
    }
}
