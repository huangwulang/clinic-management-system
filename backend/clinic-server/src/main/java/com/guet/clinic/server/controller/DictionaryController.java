package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.DictionaryItem;
import com.guet.clinic.server.service.DictionaryItemService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/dictionaries/{type}/items")
public class DictionaryController {
    private final DictionaryItemService service;
    public DictionaryController(DictionaryItemService service) { this.service = service; }
    @GetMapping public Result<List<DictionaryItem>> list(@PathVariable String type) {
        return Result.success(service.listByType(type));
    }
    @PostMapping public Result<DictionaryItem> create(@PathVariable String type, @RequestBody DictionaryItem item) {
        item.setDictType(type);
        return Result.success(service.save(item));
    }
    @PutMapping("/{id}") public Result<DictionaryItem> update(@PathVariable String type, @PathVariable Long id, @RequestBody DictionaryItem item) {
        item.setDictType(type);
        return Result.success(service.update(id, item));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { service.delete(id); return Result.ok(); }
}
