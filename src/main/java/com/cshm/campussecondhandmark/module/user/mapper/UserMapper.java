package com.cshm.campussecondhandmark.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
