package com.rachel.seckill.controller;

import com.rachel.seckill.domain.SeckillUser;
import com.rachel.seckill.result.CodeMsg;
import com.rachel.seckill.result.Result;
import com.rachel.seckill.service.SeckillUserService;
import com.rachel.seckill.util.MD5Util;
import com.rachel.seckill.util.ValidatorUtil;
import com.rachel.seckill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    SeckillUserService seckillUserService;
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/to_login")
    String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        //参数校验
//        String password = loginVo.getPassword();
//        String mobile = loginVo.getMobile();
//        if (StringUtils.isEmpty(password)) {
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if (StringUtils.isEmpty(mobile)) {
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if (!ValidatorUtil.isMobile(mobile)) {
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
        //登录
//        CodeMsg cm = seckillUserService.login(loginVo);
          seckillUserService.login(response, loginVo);
//        String token = seckillUserService.login(response, loginVo);
//        if (cm.getCode() == 0) {
//            return Result.success(true);
//        }
//        return Result.error(cm);
        return Result.success(true);
    }
}
