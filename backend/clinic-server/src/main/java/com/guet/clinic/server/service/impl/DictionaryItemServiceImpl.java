package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.DictionaryItem;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.DictionaryItemMapper;
import com.guet.clinic.server.service.DictionaryItemService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DictionaryItemServiceImpl extends AbstractCrudService<DictionaryItem> implements DictionaryItemService {
    @Autowired
    private DictionaryItemMapper dictionaryItemMapper;

    @Override
    protected CrudMapper<DictionaryItem> mapper() {
        return dictionaryItemMapper;
    }

    @Override
    public List<DictionaryItem> listByType(String dictType) {
        return dictionaryItemMapper.selectByType(dictType);
    }
}
