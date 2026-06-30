package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.dto.StatusUpdateDTO;
import com.guet.clinic.pojo.entity.Drug;
import com.guet.clinic.server.service.DrugService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drugs")
public class DrugController extends CrudController<Drug> {
    private final DrugService drugService;
    public DrugController(DrugService drugService) { this.drugService = drugService; }
    @Override protected DrugService service() { return drugService; }
    @GetMapping("/search")
    public Result<PageResult<Drug>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(drugService.search(page, size, keyword, category, status, startDate, endDate));
    }
    @GetMapping("/next-code") public Result<String> nextCode() { return Result.success(drugService.nextDrugCode()); }
    @PostMapping("/{id}/enable") public Result<Drug> enable(@PathVariable Long id) { return Result.success(drugService.enable(id)); }
    @PostMapping("/{id}/disable") public Result<Drug> disable(@PathVariable Long id) { return Result.success(drugService.disable(id)); }
    @PostMapping("/{id}/copy") public Result<Drug> copy(@PathVariable Long id) { return Result.success(drugService.copy(id)); }
    @PutMapping("/{id}/status")
    public Result<Drug> status(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        return Result.success("DISABLED".equalsIgnoreCase(dto.getStatus()) ? drugService.disable(id) : drugService.enable(id));
    }
    @GetMapping("/saleable")
    public Result<List<Map<String, Object>>> saleable(@RequestParam(required = false) String keyword) {
        return Result.success(drugService.saleable(keyword));
    }
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<Drug>> importCsv(@RequestPart("file") MultipartFile file) { return Result.success(drugService.importCsv(file)); }
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=drugs.csv")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(drugService.exportCsv(keyword));
    }
}
