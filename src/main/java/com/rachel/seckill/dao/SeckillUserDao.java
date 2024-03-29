package com.rachel.seckill.dao;

import com.rachel.seckill.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillUserDao {

    @Select("select * from seckill_user where id = #{id}")
    public SeckillUser getById(@Param("id") long id);

    @Update("update seckill_user set password = #{password}")
    public void update(SeckillUser toBeUpdate);
}
