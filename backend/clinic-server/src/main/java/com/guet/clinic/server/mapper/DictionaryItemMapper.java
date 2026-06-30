package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.DictionaryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DictionaryItemMapper extends CrudMapper<DictionaryItem> {
    List<DictionaryItem> selectByType(@Param("dictType") String dictType);
}
