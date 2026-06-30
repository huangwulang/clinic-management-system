package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.BaseEntity;
import com.guet.clinic.server.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public abstract class CrudController<T extends BaseEntity> {
            protected abstract CrudService<T> service();

    @GetMapping
    public Result<PageResult<T>> page(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "keyword", required = false) String keyword) {
        return Result.ok(service().page(page, size, keyword));
    }

    @GetMapping("/list")
    public Result<List<T>> list(@RequestParam(name = "keyword", required = false) String keyword) { return Result.ok(service().list(keyword)); }

    @GetMapping("/{id}")
    public Result<T> get(@PathVariable("id") Long id) { return Result.ok(service().get(id)); }

    @PostMapping
    public Result<T> create(@Valid @RequestBody T entity) { return Result.ok(service().save(entity)); }

    @PutMapping("/{id}")
    public Result<T> update(@PathVariable("id") Long id, @RequestBody T entity) { return Result.ok(service().update(id, entity)); }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) { service().delete(id); return Result.ok(); }
}
