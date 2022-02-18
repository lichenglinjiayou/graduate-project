package com.lichenglin.gulimall.product.service.impl;

import com.lichenglin.common.to.SkuReductionTo;
import com.lichenglin.gulimall.product.entity.SkuImagesEntity;
import com.lichenglin.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.lichenglin.gulimall.product.entity.SpuInfoEntity;
import com.lichenglin.gulimall.product.feign.SpuFeign;
import com.lichenglin.gulimall.product.service.SkuImagesService;
import com.lichenglin.gulimall.product.service.SkuSaleAttrValueService;
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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.SkuInfoDao;
import com.lichenglin.gulimall.product.entity.SkuInfoEntity;
import com.lichenglin.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

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
        if(skus.size() > 0){
            skus.forEach((item)->{
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                List<String> imagesUrl = new ArrayList<>();
                item.getImages().forEach((img)->{
                    if(img.getDefaultImg() == 1){
                        String imgUrl = img.getImgUrl();
                        imagesUrl.add(imgUrl);
                    }
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    //DO: 没有选中的图片，不进行保存；
                    if(!StringUtils.isEmpty(skuImagesEntity.getImgUrl())){
                        imagesEntities.add(skuImagesEntity);
                    }
                });
                skuInfoEntity.setSkuDefaultImg(String.join(",",imagesUrl));
                this.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                imagesEntities.forEach((img)->{
                    img.setSkuId(skuId);
                });
                //保存销售属性信息
                item.getAttr().forEach((attr)->{
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr,skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    saleAttrValueEntities.add(skuSaleAttrValueEntity);
                });
                //3 远程调用保存sku优惠信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount()>0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1){
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
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("sku_id",key).or().eq("spu_id",key).or().like("sku_name",key).or()
                        .like("sku_desc",key);
            });
        }
        if(!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        if(!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }
        if(!StringUtils.isEmpty(max)){
            try {
                BigDecimal maxDecimal = new BigDecimal(max);
                if(maxDecimal.compareTo(new BigDecimal(0))==1){
                    wrapper.le("price",max);
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

}