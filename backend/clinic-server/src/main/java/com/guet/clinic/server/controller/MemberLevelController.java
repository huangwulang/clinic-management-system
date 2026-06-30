package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.DictionaryItem;
import com.guet.clinic.server.service.DictionaryItemService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/member-levels")
public class MemberLevelController {
    private final DictionaryItemService service;
    public MemberLevelController(DictionaryItemService service) { this.service = service; }
    @GetMapping public Result<List<DictionaryItem>> list() {
        return Result.success(service.list(null).stream().filter(v -> "MEMBER_LEVEL".equalsIgnoreCase(v.getDictType())).toList());
    }
    @PostMapping public Result<DictionaryItem> create(@RequestBody DictionaryItem value) {
        value.setDictType("MEMBER_LEVEL");
        return Result.success(service.save(value));
    }
    @PutMapping("/{id}") public Result<DictionaryItem> update(@PathVariable Long id, @RequestBody DictionaryItem value) {
        value.setDictType("MEMBER_LEVEL");
        return Result.success(service.update(id, value));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { service.delete(id); return Result.ok(); }
}
