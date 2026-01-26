package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    
    @Select("select * from user where openid = #{openId}")
    User getByOpenId(Long openId);

    void insert(User user);

    Integer getSum(LocalDateTime beginTime, LocalDateTime endTime);
}
