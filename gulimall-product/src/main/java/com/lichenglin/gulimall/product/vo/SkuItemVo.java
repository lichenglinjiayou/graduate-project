package com.lichenglin.gulimall.product.vo;

import com.lichenglin.gulimall.product.entity.SkuImagesEntity;
import com.lichenglin.gulimall.product.entity.SkuInfoEntity;
import com.lichenglin.gulimall.product.entity.SpuInfoDescEntity;
import com.lichenglin.gulimall.product.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //1. 查询skuInfo表，获取sku的基本信息；
    SkuInfoEntity skuInfoEntity;
    //2. 查询skuImags表，获取sku的图片信息；
    List<SkuImagesEntity> images;
    //3. 查询`pms_sku_sale_attr_value`表，获取当前sku下存在多少种spu的组合信息；
    List<SkuItemSaleAttrsVo> saleAttr;
    //4. 查询`pms_spu_info`，获取spu的介绍信息；
    SpuInfoDescEntity spuInfoDescEntity;
    //5. 查询`pms_product_attr_value`表，获取规格信息；
    List<SpuItemBaseAttrsGroupVo> groupAttrs;
    //6. 查看是否有货
    boolean hasStock = true;

    //7.秒杀信息
    SecKillInfoVo secKillInfoVo;


    @Data
    public static class SkuItemSaleAttrsVo{
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }
    @Data
    public static class SpuItemBaseAttrsGroupVo{
        private String groupName;
        private List<SpuBaseAttrsVo> attrs;
    }

    @Data
    public static class SpuBaseAttrsVo{
        private String attrName;
        private List<String> attrValue;
    }
}
