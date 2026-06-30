package com.guet.clinic.server.service;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.PriceAdjustment;
import com.guet.clinic.pojo.vo.PriceAdjustmentVO;

import java.util.List;

public interface PriceAdjustmentService extends CrudService<PriceAdjustment> {
    PageResult<PriceAdjustmentVO> adjustableDrugs(int page, int size, String keyword, String category);
    PageResult<PriceAdjustmentVO> recordSummaries(int page, int size, String keyword, String category);
    List<PriceAdjustmentVO> formRows(Long drugId);
    List<PriceAdjustmentVO> history(Long drugId);
    PriceAdjustment complete(PriceAdjustment request);
}
