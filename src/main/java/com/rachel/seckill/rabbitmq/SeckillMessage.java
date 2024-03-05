package com.rachel.seckill.rabbitmq;

import com.rachel.seckill.domain.SeckillUser;

public class SeckillMessage {
    private SeckillUser user;
    private long goodId;

    public SeckillUser getUser() {
        return user;
    }

    public void setUser(SeckillUser user) {
        this.user = user;
    }

    public long getGoodId() {
        return goodId;
    }

    public void setGoodId(long goodId) {
        this.goodId = goodId;
    }
}
