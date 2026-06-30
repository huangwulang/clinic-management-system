package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.DictionaryItem;
import java.util.List;

public interface DictionaryItemService extends CrudService<DictionaryItem> {
    List<DictionaryItem> listByType(String dictType);
}
