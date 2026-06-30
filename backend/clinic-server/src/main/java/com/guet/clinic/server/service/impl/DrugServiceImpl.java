package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.Drug;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.DrugMapper;
import com.guet.clinic.server.service.DrugService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DrugServiceImpl extends AbstractCrudService<Drug> implements DrugService {
    private final DrugMapper drugMapper;
    public DrugServiceImpl(DrugMapper drugMapper) { this.drugMapper = drugMapper; }
    @Override protected CrudMapper<Drug> mapper() { return drugMapper; }

    @Override
    public PageResult<Drug> search(int page, int size, String keyword, String category,
                                   String status, String startDate, String endDate) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        String safeKeyword = normalizeFilter(keyword);
        String safeCategory = normalizeFilter(category);
        String safeStatus = normalizeFilter(status);
        String safeStartDate = normalizeFilter(startDate);
        String safeEndDate = normalizeFilter(endDate);
        return PageResult.of(
                drugMapper.selectPageByFilters(offset, safeSize, safeKeyword, safeCategory,
                        safeStatus, safeStartDate, safeEndDate),
                drugMapper.countByFilters(safeKeyword, safeCategory, safeStatus,
                        safeStartDate, safeEndDate),
                safePage,
                safeSize
        );
    }

    @Override
    public String nextDrugCode() {
        Long maxCode = drugMapper.selectMaxNumericDrugCode();
        return String.valueOf(maxCode == null ? 1000001L : maxCode + 1L);
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Override
    @Transactional
    public synchronized Drug save(Drug drug) {
        String drugCode = normalizeDrugCode(drug.getDrugCode());
        if (drugCode == null) {
            drugCode = nextDrugCode();
        }
        ensureDrugCodeAvailable(drugCode, null);
        drug.setDrugCode(drugCode);
        try {
            return super.save(drug);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("药品编码已存在，请使用其他编码");
        }
    }

    @Override
    @Transactional
    public Drug update(Long id, Drug drug) {
        String drugCode = normalizeDrugCode(drug.getDrugCode());
        if (drugCode != null) {
            ensureDrugCodeAvailable(drugCode, id);
            drug.setDrugCode(drugCode);
        }
        try {
            return super.update(id, drug);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("药品编码已存在，请使用其他编码");
        }
    }

    @Override @Transactional public Drug enable(Long id) { return status(id, BusinessStatusConstant.ENABLED); }
    @Override @Transactional public Drug disable(Long id) { return status(id, BusinessStatusConstant.DISABLED); }
    @Override
    public List<Map<String, Object>> saleable(String keyword) {
        return drugMapper.selectSaleableDrugs(normalizeFilter(keyword));
    }

    private Drug status(Long id, String status) {
        Drug drug = get(id);
        drug.setStatus(status);
        drug.setUpdatedAt(LocalDateTime.now());
        drugMapper.update(drug);
        return get(id);
    }
    @Override @Transactional
    public Drug copy(Long id) {
        Drug source = get(id);
        Drug drug = new Drug();
        drug.setDrugCode(nextDrugCode());
        drug.setBarcode(source.getBarcode());
        drug.setName(source.getName() + "副本");
        drug.setPinyin(source.getPinyin());
        drug.setSpecification(source.getSpecification());
        drug.setCategory(source.getCategory());
        drug.setDosageForm(source.getDosageForm());
        drug.setOtc(source.getOtc());
        drug.setInvoiceItem(source.getInvoiceItem());
        drug.setUnit(source.getUnit());
        drug.setPackUnit(source.getPackUnit());
        drug.setBaseRatio(source.getBaseRatio());
        drug.setBaseUnit(source.getBaseUnit());
        drug.setDoseRatio(source.getDoseRatio());
        drug.setDoseUnit(source.getDoseUnit());
        drug.setPurchasePrice(source.getPurchasePrice());
        drug.setSellPrice(source.getSellPrice());
        drug.setManufacturer(source.getManufacturer());
        drug.setApprovalNo(source.getApprovalNo());
        drug.setStatus(source.getStatus());
        drug.setWarningStock(source.getWarningStock());
        drug.setStockMin(source.getStockMin());
        drug.setStockMax(source.getStockMax());
        drug.setLocationNo(source.getLocationNo());
        drug.setExpiryWarningDays(source.getExpiryWarningDays());
        drug.setAllowSplit(source.getAllowSplit());
        drug.setAllowMemberDiscount(source.getAllowMemberDiscount());
        drug.setUsageMethod(source.getUsageMethod());
        drug.setSingleDose(source.getSingleDose());
        drug.setFrequency(source.getFrequency());
        drug.setDefaultDays(source.getDefaultDays());
        drug.setDefaultTotal(source.getDefaultTotal());
        drug.setTotalUnit(source.getTotalUnit());
        drug.setDescription(source.getDescription());
        drug.setRemark(source.getRemark());
        return save(drug);
    }
    @Override @Transactional
    public List<Drug> importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择CSV文件");
        try {
            String[] lines = new String(file.getBytes(), StandardCharsets.UTF_8).replace("\r", "").split("\n");
            List<Drug> result = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) continue;
                String[] cells = lines[i].split(",", -1);
                if (cells.length < 11) throw new BusinessException("CSV第" + (i + 1) + "行字段不足");
                Drug drug = new Drug();
                drug.setDrugCode(cells[0].trim());
                drug.setName(cells[1].trim());
                drug.setSpecification(cells[2].trim());
                drug.setCategory(cells[3].trim());
                drug.setDosageForm(cells[4].trim());
                drug.setUnit(cells[5].trim());
                drug.setPurchasePrice(decimal(cells[6]));
                drug.setSellPrice(decimal(cells[7]));
                drug.setManufacturer(cells[8].trim());
                drug.setApprovalNo(cells[9].trim());
                drug.setStatus(cells[10].isBlank() ? BusinessStatusConstant.ENABLED : cells[10].trim());
                if (cells.length > 11 && !cells[11].isBlank()) drug.setWarningStock(Integer.valueOf(cells[11].trim()));
                result.add(save(drug));
            }
            return result;
        } catch (IOException | NumberFormatException ex) {
            throw new BusinessException("CSV文件解析失败：" + ex.getMessage());
        }
    }
    @Override
    public byte[] exportCsv(String keyword) {
        StringBuilder csv = new StringBuilder("\uFEFF药品编码,药品名称,规格,分类,剂型,单位,采购价,零售价,生产厂家,批准文号,状态,预警库存\n");
        for (Drug drug : list(keyword)) {
            csv.append(cell(drug.getDrugCode())).append(',').append(cell(drug.getName())).append(',')
                .append(cell(drug.getSpecification())).append(',').append(cell(drug.getCategory())).append(',')
                .append(cell(drug.getDosageForm())).append(',').append(cell(drug.getUnit())).append(',')
                .append(cell(drug.getPurchasePrice())).append(',').append(cell(drug.getSellPrice())).append(',')
                .append(cell(drug.getManufacturer())).append(',').append(cell(drug.getApprovalNo())).append(',')
                .append(cell(drug.getStatus())).append(',').append(cell(drug.getWarningStock())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    private BigDecimal decimal(String value) { return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value.trim()); }
    private String normalizeDrugCode(String drugCode) {
        if (drugCode == null || drugCode.isBlank()) {
            return null;
        }
        return drugCode.trim();
    }
    private void ensureDrugCodeAvailable(String drugCode, Long currentId) {
        Drug existing = drugMapper.selectByDrugCode(drugCode);
        if (existing != null && (currentId == null || !currentId.equals(existing.getId()))) {
            throw new BusinessException("药品编码已存在，请使用其他编码");
        }
    }
    private String cell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
