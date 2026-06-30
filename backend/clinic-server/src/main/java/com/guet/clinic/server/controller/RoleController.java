package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.Role;
import com.guet.clinic.server.service.RoleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleController extends CrudController<Role> {
    private final RoleService roleService;
    private final ObjectMapper objectMapper;
    public RoleController(RoleService roleService, ObjectMapper objectMapper) {
        this.roleService = roleService;
        this.objectMapper = objectMapper;
    }
    @Override protected RoleService service() { return roleService; }
    @PutMapping("/{id}/permissions")
    public Result<Role> permissions(@PathVariable Long id, @RequestBody Map<String, List<String>> body) throws JsonProcessingException {
        Role role = new Role();
        role.setPermissions(objectMapper.writeValueAsString(body.getOrDefault("permissions", List.of())));
        return Result.success(roleService.update(id, role));
    }
}
