package com.guet.clinic.server.service;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.Drug;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface DrugService extends CrudService<Drug> {
    PageResult<Drug> search(int page, int size, String keyword, String category,
                            String status, String startDate, String endDate);
    String nextDrugCode();
    Drug enable(Long id);
    Drug disable(Long id);
    Drug copy(Long id);
    List<Map<String, Object>> saleable(String keyword);
    List<Drug> importCsv(MultipartFile file);
    byte[] exportCsv(String keyword);
}
