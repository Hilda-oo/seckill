package com.rachel.seckill.redis;

public class AccessKey extends BasePrefix{

    private AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey withExpire(int seconds) {
        return new AccessKey(seconds, "access");
    }
}
