package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.SystemMessage;
import com.guet.clinic.server.service.SystemMessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class SystemMessageController extends CrudController<SystemMessage> {
    private final SystemMessageService service;
    public SystemMessageController(SystemMessageService service) { this.service = service; }
    @Override protected SystemMessageService service() { return service; }

    @PutMapping("/{id}/read")
    public Result<SystemMessage> read(@PathVariable Long id) { return Result.success(service.markRead(id)); }

    @PutMapping("/read-all")
    public Result<Void> readAll(@RequestParam(required = false) Long receiverId) {
        service.markAllRead(receiverId);
        return Result.ok();
    }
}
