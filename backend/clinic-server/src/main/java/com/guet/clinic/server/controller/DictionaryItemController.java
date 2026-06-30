package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.DictionaryItem;
import com.guet.clinic.server.service.DictionaryItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dictionary-items")
public class DictionaryItemController extends CrudController<DictionaryItem> {
    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Override
    protected DictionaryItemService service() {
        return dictionaryItemService;
    }
}
