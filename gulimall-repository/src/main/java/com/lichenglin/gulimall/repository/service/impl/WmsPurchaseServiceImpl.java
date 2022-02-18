package com.lichenglin.gulimall.repository.service.impl;

import com.lichenglin.common.constant.RepositoryConstant;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.repository.entity.WmsPurchaseDetailEntity;
import com.lichenglin.gulimall.repository.entity.WmsWareSkuEntity;
import com.lichenglin.gulimall.repository.feign.ProductFeignService;
import com.lichenglin.gulimall.repository.service.WmsPurchaseDetailService;
import com.lichenglin.gulimall.repository.service.WmsWareSkuService;
import com.lichenglin.gulimall.repository.vo.MergeVo;
import com.lichenglin.gulimall.repository.vo.PurchaseFinishVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.repository.dao.WmsPurchaseDao;
import com.lichenglin.gulimall.repository.entity.WmsPurchaseEntity;
import com.lichenglin.gulimall.repository.service.WmsPurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("wmsPurchaseService")
public class WmsPurchaseServiceImpl extends ServiceImpl<WmsPurchaseDao, WmsPurchaseEntity> implements WmsPurchaseService {

    @Autowired
    private WmsPurchaseDetailService wmsPurchaseDetailService;
    @Autowired
    private WmsWareSkuService wmsWareSkuService;
    @Autowired
    private ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsPurchaseEntity> page = this.page(
                new Query<WmsPurchaseEntity>().getPage(params),
                new QueryWrapper<WmsPurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPurchaseByStatus(Map<String, Object> params) {

        IPage<WmsPurchaseEntity> page = this.page(
                new Query<WmsPurchaseEntity>().getPage(params),
                new QueryWrapper<WmsPurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){
            WmsPurchaseEntity wmsPurchaseEntity = new WmsPurchaseEntity();
            wmsPurchaseEntity.setCreateTime(new Date());
            wmsPurchaseEntity.setUpdateTime(new Date());
            wmsPurchaseEntity.setStatus(RepositoryConstant.PurchaseStatusEnum.CREATE_STATUS.getCode());
            this.save(wmsPurchaseEntity);
            purchaseId = wmsPurchaseEntity.getId();
        }

        Long finalPurchaseId = purchaseId;
        List<WmsPurchaseDetailEntity> list = new ArrayList<>();
        mergeVo.getItems().forEach((item)->{
            WmsPurchaseDetailEntity purchaseDetailEntity = wmsPurchaseDetailService.getById(item);
            if(purchaseDetailEntity.getStatus() == RepositoryConstant.PurchaseDetailStatusEnum.CREATE_STATUS.getCode() ||
                purchaseDetailEntity.getStatus() == RepositoryConstant.PurchaseDetailStatusEnum.ASSIGN_STATUS.getCode()){
                WmsPurchaseDetailEntity entity = new WmsPurchaseDetailEntity();
                entity.setId(item);
                entity.setPurchaseId(finalPurchaseId);
                entity.setStatus(RepositoryConstant.PurchaseDetailStatusEnum.ASSIGN_STATUS.getCode());
                list.add(entity);
            }
        });
        wmsPurchaseDetailService.updateBatchById(list);

        WmsPurchaseEntity wmsPurchaseEntity = new WmsPurchaseEntity();
        wmsPurchaseEntity.setId(purchaseId);
        wmsPurchaseEntity.setUpdateTime(new Date());
        this.updateById(wmsPurchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        List<WmsPurchaseEntity> wmsPurchaseEntities = new ArrayList<>();
        ids.forEach((item)->{
            WmsPurchaseEntity entity = this.getById(item);
            if(entity.getStatus() == RepositoryConstant.PurchaseStatusEnum.CREATE_STATUS.getCode() ||
                entity.getStatus() == RepositoryConstant.PurchaseStatusEnum.ASSIGN_STATUS.getCode()){
                wmsPurchaseEntities.add(entity);
            }
        });

        wmsPurchaseEntities.forEach((item)->{
            item.setStatus(RepositoryConstant.PurchaseStatusEnum.RECEIVE_STATUS.getCode());
            item.setUpdateTime(new Date());
        });

        this.updateBatchById(wmsPurchaseEntities);

        //更改每个采购单中所有采购项的状态
        wmsPurchaseEntities.forEach((item)->{
            Long id = item.getId();
            List<WmsPurchaseDetailEntity> purchaseDetailEntities = wmsPurchaseDetailService.list(new QueryWrapper<WmsPurchaseDetailEntity>().eq("purchase_id", id));

            List<WmsPurchaseDetailEntity> list = new ArrayList<>();
            purchaseDetailEntities.forEach((entity)->{
                entity.setStatus(RepositoryConstant.PurchaseDetailStatusEnum.RECEIVE_STATUS.getCode());
                list.add(entity);
            });
            wmsPurchaseDetailService.updateBatchById(list);
        });
    }

    @Transactional
    @Override
    public void finshed(PurchaseFinishVo purchaseFinishVo) {
        // 改变采购单状态
        Long id = purchaseFinishVo.getId();
        WmsPurchaseEntity purchaseEntity = this.getById(id);
        List<WmsPurchaseDetailEntity> success = new ArrayList<>();
        List<WmsPurchaseDetailEntity> fail = new ArrayList<>();
        //改变采购单中的每个采购项的状态
        purchaseFinishVo.getItems().forEach((item)->{
            Long itemId = item.getItemId();
            WmsPurchaseDetailEntity entity = wmsPurchaseDetailService.getById(itemId);
            if(item.getStatus() == RepositoryConstant.PurchaseDetailStatusEnum.FINISH.getCode()){
                entity.setStatus(RepositoryConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                success.add(entity);
                List<WmsWareSkuEntity> list = wmsWareSkuService.list(new QueryWrapper<WmsWareSkuEntity>().eq("sku_id", entity.getSkuId())
                        .eq("ware_id", entity.getWareId()));
                if(list.size() == 0 || list == null){
                    WmsWareSkuEntity wmsWareSkuEntity = new WmsWareSkuEntity();
                    BeanUtils.copyProperties(entity,wmsWareSkuEntity);
                    wmsWareSkuEntity.setStock(entity.getSkuNum());
                    wmsWareSkuEntity.setStockLocked(0);
                    try {
                        wmsWareSkuEntity.setSkuName(((Map<String,Object>)productFeignService.info(entity.getSkuId()).get("skuInfo")).get("skuName")+"");
                    } catch (Exception e) {
                    }
                    wmsWareSkuService.save(wmsWareSkuEntity);
                }else{
                    WmsWareSkuEntity wmsWareSkuEntity = new WmsWareSkuEntity();
                    BeanUtils.copyProperties(entity,wmsWareSkuEntity);
                    wmsWareSkuEntity.setStock(entity.getSkuNum());
                    wmsWareSkuService.saveSkuInfo(wmsWareSkuEntity);
                }

            }else{
                entity.setStatus(RepositoryConstant.PurchaseDetailStatusEnum.ERROR_STATUS.getCode());
                fail.add(entity);
            }
        });
        if(fail.size() == 0){
            purchaseEntity.setStatus(RepositoryConstant.PurchaseStatusEnum.FINISH.getCode());
            purchaseEntity.setUpdateTime(new Date());
        }else{
            purchaseEntity.setStatus(RepositoryConstant.PurchaseStatusEnum.ERROR_STATUS.getCode());
            purchaseEntity.setUpdateTime(new Date());
        }

        this.updateById(purchaseEntity);
        wmsPurchaseDetailService.updateBatchById(success);
        //将成功的采购进行入库
    }


}