package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.Drug;
import com.guet.clinic.pojo.entity.PriceAdjustment;
import com.guet.clinic.pojo.vo.PriceAdjustmentVO;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.DrugMapper;
import com.guet.clinic.server.mapper.PriceAdjustmentMapper;
import com.guet.clinic.server.service.PriceAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceAdjustmentServiceImpl extends AbstractCrudService<PriceAdjustment>
        implements PriceAdjustmentService {

    @Autowired
    private PriceAdjustmentMapper priceAdjustmentMapper;

    @Autowired
    private DrugMapper drugMapper;

    @Override
    protected CrudMapper<PriceAdjustment> mapper() {
        return priceAdjustmentMapper;
    }

    @Override
    public PageResult<PriceAdjustmentVO> adjustableDrugs(
            int page, int size, String keyword, String category) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        String normalizedCategory = normalizeCategory(category);
        return PageResult.of(
                priceAdjustmentMapper.selectAdjustableDrugs(
                        offset, safeSize, keyword, normalizedCategory),
                priceAdjustmentMapper.countAdjustableDrugs(keyword, normalizedCategory),
                safePage,
                safeSize
        );
    }

    @Override
    public PageResult<PriceAdjustmentVO> recordSummaries(
            int page, int size, String keyword, String category) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        String normalizedCategory = normalizeCategory(category);
        return PageResult.of(
                priceAdjustmentMapper.selectRecordSummaries(
                        offset, safeSize, keyword, normalizedCategory),
                priceAdjustmentMapper.countRecordSummaries(keyword, normalizedCategory),
                safePage,
                safeSize
        );
    }

    @Override
    public List<PriceAdjustmentVO> formRows(Long drugId) {
        requireDrug(drugId);
        return priceAdjustmentMapper.selectFormRows(drugId);
    }

    @Override
    public List<PriceAdjustmentVO> history(Long drugId) {
        requireDrug(drugId);
        return priceAdjustmentMapper.selectHistoryByDrugId(drugId);
    }

    @Override
    @Transactional
    public PriceAdjustment complete(PriceAdjustment request) {
        if (request == null || request.getDrugId() == null) {
            throw new BusinessException("请选择需要调价的药品");
        }

        Drug drug = requireDrug(request.getDrugId());
        BigDecimal oldPurchasePrice = valueOrZero(drug.getPurchasePrice());
        BigDecimal oldSellPrice = valueOrZero(drug.getSellPrice());
        BigDecimal newPurchasePrice = request.getNewPurchasePrice() == null
                ? oldPurchasePrice : request.getNewPurchasePrice();
        BigDecimal newSellPrice = request.getNewSellPrice();

        if (newPurchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("采购价不能小于0");
        }
        if (newSellPrice == null || newSellPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("现零售价必须大于0");
        }
        if (oldPurchasePrice.compareTo(newPurchasePrice) == 0
                && oldSellPrice.compareTo(newSellPrice) == 0) {
            throw new BusinessException("调整后的价格不能与当前价格相同");
        }

        PriceAdjustment adjustment = new PriceAdjustment();
        adjustment.setDrugId(drug.getId());
        adjustment.setDrugCode(drug.getDrugCode());
        adjustment.setDrugName(drug.getName());
        adjustment.setOldPurchasePrice(oldPurchasePrice);
        adjustment.setNewPurchasePrice(newPurchasePrice);
        adjustment.setOldSellPrice(oldSellPrice);
        adjustment.setNewSellPrice(newSellPrice);
        adjustment.setOperator(hasText(request.getOperator())
                ? request.getOperator().trim() : "系统管理员");
        adjustment.setReason(hasText(request.getReason())
                ? request.getReason().trim() : "药品调价");

        LocalDateTime now = LocalDateTime.now();
        adjustment.setCreatedAt(now);
        adjustment.setUpdatedAt(now);
        adjustment.setDeleted(false);
        priceAdjustmentMapper.insert(adjustment);
        priceAdjustmentMapper.updateDrugPrices(
                drug.getId(), newPurchasePrice, newSellPrice, now);
        priceAdjustmentMapper.updateInventoryAmounts(
                drug.getId(), newPurchasePrice, newSellPrice, now);
        return adjustment;
    }

    private Drug requireDrug(Long drugId) {
        Drug drug = drugId == null ? null : drugMapper.selectById(drugId);
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        return drug;
    }

    private String normalizeCategory(String category) {
        return "中药".equals(category) || "西/成药".equals(category) ? category : null;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
