package com.lichenglin.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.to.SkuReductionTo;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.product.config.MyThreadConfig;
import com.lichenglin.gulimall.product.entity.*;
import com.lichenglin.gulimall.product.feign.CouponFeign;
import com.lichenglin.gulimall.product.feign.SpuFeign;
import com.lichenglin.gulimall.product.service.*;
import com.lichenglin.gulimall.product.vo.SecKillInfoVo;
import com.lichenglin.gulimall.product.vo.SkuItemVo;
import com.lichenglin.gulimall.product.vo.spu.SkuItemSaleAttrsVo;
import com.lichenglin.gulimall.product.vo.spu.Skus;
import com.lichenglin.gulimall.product.vo.spu.SpuSaveVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.SkuInfoDao;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    CouponFeign couponFeign;


    @Autowired
    SpuFeign spuFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SpuInfoEntity spuInfoEntity, SpuSaveVo spuSaveVo) {
        List<Skus> skus = spuSaveVo.getSkus();
        List<SkuImagesEntity> imagesEntities = new ArrayList<>();
        List<SkuSaleAttrValueEntity> saleAttrValueEntities = new ArrayList<>();
        if (skus.size() > 0) {
            skus.forEach((item) -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                List<String> imagesUrl = new ArrayList<>();
                item.getImages().forEach((img) -> {
                    if (img.getDefaultImg() == 1) {
                        String imgUrl = img.getImgUrl();
                        imagesUrl.add(imgUrl);
                    }
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    //DO: 没有选中的图片，不进行保存；
                    if (!StringUtils.isEmpty(skuImagesEntity.getImgUrl())) {
                        imagesEntities.add(skuImagesEntity);
                    }
                });
                skuInfoEntity.setSkuDefaultImg(String.join(",", imagesUrl));
                this.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                imagesEntities.forEach((img) -> {
                    img.setSkuId(skuId);
                });
                //保存销售属性信息
                item.getAttr().forEach((attr) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    saleAttrValueEntities.add(skuSaleAttrValueEntity);
                });
                //3 远程调用保存sku优惠信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
                    spuFeign.saveSkuReduction(skuReductionTo);
                }
            });

            skuImagesService.saveBatch(imagesEntities);
            skuSaleAttrValueService.saveBatch(saleAttrValueEntities);
        }


    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        String key = (String) params.get("key");
        String catalogId = (String) params.get("catalogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().eq("spu_id", key).or().like("sku_name", key).or()
                        .like("sku_desc", key);
            });
        }
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal maxDecimal = new BigDecimal(max);
                if (maxDecimal.compareTo(new BigDecimal(0)) == 1) {
                    wrapper.le("price", max);
                }
            } catch (Exception e) {

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo getSkuInfo(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        //线程池+异步
        CompletableFuture<SkuInfoEntity> skuInfo = CompletableFuture.supplyAsync(() -> {
            //1. 查询skuInfo表，获取sku的基本信息；
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setSkuInfoEntity(skuInfoEntity);
            return skuInfoEntity;
        }, executor);
        CompletableFuture<Void> attrInfo = skuInfo.thenAcceptAsync((skuInfoEntity) -> {
            Long spuId = skuInfoEntity.getSpuId();
            //3. 查询`pms_sku_sale_attr_value`表，获取当前sku下存在多少种spu的组合信息；
            List<SkuItemSaleAttrsVo> skuItemSaleAttrsVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
            List<SkuItemVo.SkuItemSaleAttrsVo> skuItemSaleAttrsVos1 = new ArrayList<>();
            skuItemSaleAttrsVos.forEach((item) -> {
                SkuItemVo.SkuItemSaleAttrsVo skuItemSaleAttrsVo = new SkuItemVo.SkuItemSaleAttrsVo();
                skuItemSaleAttrsVo.setAttrId(item.getAttrId());
                skuItemSaleAttrsVo.setAttrName(item.getAttrName());
                skuItemSaleAttrsVo.setAttrValues(item.getAttrValues());
                skuItemSaleAttrsVos1.add(skuItemSaleAttrsVo);
            });
            skuItemVo.setSaleAttr(skuItemSaleAttrsVos1);
        }, executor);

        CompletableFuture<Void> descInfo = skuInfo.thenAcceptAsync((skuInfoEntity -> {
            Long spuId = skuInfoEntity.getSpuId();
            //4. 查询`pms_spu_info`，获取spu的介绍信息；
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
            skuItemVo.setSpuInfoDescEntity(spuInfoDescEntity);
        }), executor);


        CompletableFuture<Void> groupInfo = skuInfo.thenAcceptAsync((skuInfoEntity -> {
            Long spuId = skuInfoEntity.getSpuId();
            Long catalogId = skuInfoEntity.getCatalogId();
            //5. 查询`pms_product_attr_value`表，获取规格信息；
            List<SkuItemVo.SpuItemBaseAttrsGroupVo> attrsGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
            skuItemVo.setGroupAttrs(attrsGroupVos);
        }), executor);


        CompletableFuture<Void> imgInfo = CompletableFuture.runAsync(() -> {
            //2. 查询skuImags表，获取sku的图片信息；
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImagesEntities);
        }, executor);


        //TODO：查询当前sku是否参与秒杀优惠？
        CompletableFuture<Void> secKillInfo = CompletableFuture.runAsync(() -> {
            R r = couponFeign.getSkuSecInfo(skuId);
            if (r.getCode() == 0) {
                SecKillInfoVo data = r.getData(new TypeReference<SecKillInfoVo>() {
                });
                skuItemVo.setSecKillInfoVo(data);
            }
        }, executor);


        CompletableFuture<Void> allInfo = CompletableFuture.allOf(skuInfo, attrInfo, descInfo, groupInfo, imgInfo,secKillInfo);
        allInfo.join();

        return skuItemVo;


      
    }

}