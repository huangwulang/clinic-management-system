package com.guet.clinic.server.controller;

import com.guet.clinic.server.service.StockOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-out-orders")
public class StockOutOrderController extends AbstractStockDirectionController {
    private static final String STOCK_DIRECTION_OUT = "OUT";

    @Autowired
    private StockOrderService stockOrderService;

    @Override
    protected StockOrderService stockOrderService() {
        return stockOrderService;
    }

    @Override
    protected String stockDirection() {
        return STOCK_DIRECTION_OUT;
    }
}
