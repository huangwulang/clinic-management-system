package com.guet.clinic.server.controller;

import com.guet.clinic.pojo.entity.OperationLog;
import com.guet.clinic.server.service.OperationLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/operation-logs", "/system/operation-logs"})
public class OperationLogController extends CrudController<OperationLog> {
    private final OperationLogService service;
    public OperationLogController(OperationLogService service) { this.service = service; }
    @Override protected OperationLogService service() { return service; }
}
