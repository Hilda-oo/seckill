package com.rachel.seckill.redis;

public class SeckillGoodsKey extends BasePrefix{

    private SeckillGoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillGoodsKey isGoodsOver = new SeckillGoodsKey(0, "go");
    public static SeckillGoodsKey getSeckillPath = new SeckillGoodsKey(60, "sp");
    public static SeckillGoodsKey getSeckillVerifyCode = new SeckillGoodsKey(300, "vc");
}
