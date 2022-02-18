package com.lichenglin.gulimall.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.repository.entity.WmsPurchaseEntity;
import com.lichenglin.gulimall.repository.vo.MergeVo;
import com.lichenglin.gulimall.repository.vo.PurchaseFinishVo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:37:34
 */
public interface WmsPurchaseService extends IService<WmsPurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPurchaseByStatus(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void finshed(PurchaseFinishVo purchaseFinishVo);
}

