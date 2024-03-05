package com.rachel.seckill.service;

import com.rachel.seckill.dao.SeckillUserDao;
import com.rachel.seckill.domain.SeckillUser;
import com.rachel.seckill.exception.GlobalException;
import com.rachel.seckill.redis.RedisService;
import com.rachel.seckill.redis.SeckillUserKey;
import com.rachel.seckill.result.CodeMsg;
import com.rachel.seckill.util.MD5Util;
import com.rachel.seckill.util.UUIDUtil;
import com.rachel.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {
    @Autowired
    SeckillUserDao seckillUserDao;

    @Autowired
    RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    public SeckillUser getById(long id) {
        //取缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById, "" + id, SeckillUser.class);
        if (user != null) {
            return user;
        }
        //取数据库
        user = seckillUserDao.getById(id);
        if (user != null) {
            redisService.set(SeckillUserKey.getById, "" + id, user);
        }
        return user;
    }

    public boolean updatePass(long id, String password, String token) {
        //取缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById, "" + id, SeckillUser.class);
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(password, user.getSalt()));
        seckillUserDao.update(toBeUpdate);
        user.setPassword(toBeUpdate.getPassword());
        redisService.delete(SeckillUserKey.getById, "" + id);
        redisService.set(SeckillUserKey.getByToken, token, user);
        return true;
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
//            return CodeMsg.SERVER_ERROR;
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) {
//            return CodeMsg.MOBILE_NOT_EXIST;
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        String calPass = MD5Util.formPassToDBPass(password, dbSalt);
        if (!calPass.equals(dbPass)) {
//            return CodeMsg.PASSWORD_ERROR;
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        SeckillUser seckillUser = redisService.get(SeckillUserKey.getByToken, token, SeckillUser.class);
        if (seckillUser != null) {
            addCookie(response, token, seckillUser);
        }
        return seckillUser;
    }

    public void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        redisService.set(SeckillUserKey.getByToken, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(SeckillUserKey.getByToken.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
