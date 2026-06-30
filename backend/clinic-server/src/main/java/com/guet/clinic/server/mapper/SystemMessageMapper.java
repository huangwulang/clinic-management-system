package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.SystemMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemMessageMapper extends CrudMapper<SystemMessage> {
    int markRead(@Param("id") Long id);
    int markAllRead(@Param("receiverId") Long receiverId);
}
