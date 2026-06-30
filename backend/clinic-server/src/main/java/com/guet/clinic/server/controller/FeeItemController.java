package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.FeeItem;
import com.guet.clinic.server.service.FeeItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fee-items")
public class FeeItemController extends CrudController<FeeItem> {
    @Autowired
    private FeeItemService feeItemService;

    @Override
    protected FeeItemService service() {
        return feeItemService;
    }
}
