package com.lichenglin.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.constant.ProductConstant;
import com.lichenglin.common.to.SpuBoundsTo;
import com.lichenglin.common.to.es.SkuEsModel;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.product.entity.ProductAttrValueEntity;
import com.lichenglin.gulimall.product.entity.SkuInfoEntity;
import com.lichenglin.gulimall.product.entity.SpuInfoDescEntity;
import com.lichenglin.gulimall.product.feign.SearchFeign;
import com.lichenglin.gulimall.product.feign.SpuFeign;
import com.lichenglin.gulimall.product.feign.WareFeign;
import com.lichenglin.gulimall.product.service.*;
import com.lichenglin.gulimall.product.vo.SkuHasStockVo;
import com.lichenglin.gulimall.product.vo.spu.BaseAttrs;
import com.lichenglin.gulimall.product.vo.spu.Bounds;
import com.lichenglin.gulimall.product.vo.spu.SpuSaveVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.SpuInfoDao;
import com.lichenglin.gulimall.product.entity.SpuInfoEntity;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SpuFeign spuFeign;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    WareFeign wareFeign;

    @Autowired
    SearchFeign searchFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /*
        TODO: 高级部分继续完善
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        /**
         * 3 保存基本信息：spu-info; 保存描述信息：spu-info-desc; 保存图片信息：spu-image; 规格参数：produce-attr-value; 积分信息：spu-bounds
         *  保存当前spu对应的所有sku信息：sku-info; 保存sku的销售属性： sku-sale-attr; 保存sku的图片信息：sku-images;
         *  保存sku的优惠满减信息：gulimall-sms : /sku-ladder/full-reduction/member-price
         * */
        //保存描述信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //保存描述图片
        List<String> decript = spuSaveVo.getDecript();
        Long spuId = spuInfoEntity.getId();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",", decript));
        this.saveSpuDescImageInfo(spuInfoDescEntity);

        //保存图片信息
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuId, images);

        //保存spu规格参数
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        productAttrValueService.saveSpuProcutAttrValue(spuId, baseAttrs);

        //保存sku基本信息
        skuInfoService.saveSkuInfo(spuInfoEntity, spuSaveVo);

        //调用远程服务，保存商品的积分
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuId);
        spuFeign.saveSpuBounds(spuBoundsTo);
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public void saveSpuDescImageInfo(SpuInfoDescEntity spuInfoDescEntity) {
        spuInfoDescService.save(spuInfoDescEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String brandId = (String) params.get("brandId");
        String catelogId = (String) params.get("catelogId");
        String status = (String) params.get("status");
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key).or()
                        .like("spu_description", key);
            });
        }
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 7 商品上架
     *
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        // 查询商品的可以被检索的规格属性
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrId = new ArrayList<>();
        entities.forEach((item) -> {
            attrId.add(item.getAttrId());
        });
        List<Long> attrsWithSearch = attrService.selectSearch(attrId);
        Set<Long> idSet = new HashSet<>(attrsWithSearch);
        List<ProductAttrValueEntity> entitiesBySearch = new ArrayList<>();
        entities.forEach((item) -> {
            if (idSet.contains(item.getAttrId())) {
                entitiesBySearch.add(item);
            }
        });

        List<SkuEsModel.Attrs> attrsList = new ArrayList<>();
        entitiesBySearch.forEach((item) -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            attrsList.add(attrs);
        });

        // 远程微服务调用，查看是否有库存？
        List<Long> skuIds = new ArrayList<>();
        skus.forEach((item) -> {
            skuIds.add(item.getSkuId());
        });
        Map<Long, Boolean> map = new HashMap<>();
        try {
            R skusHasStock = wareFeign.getSkusHasStock(skuIds);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            List<SkuHasStockVo> data = skusHasStock.getData(typeReference);
            data.forEach((item) -> {
                map.put(item.getSkuId(), item.getHasStock());
            });
        } catch (Exception e) {
            log.error("库存远程服务调用异常：原因{}", e);
        }


        skus.forEach((sku) -> {
            SkuEsModel model = new SkuEsModel();
            BeanUtils.copyProperties(sku, model);
            /*
            7 需要单独处理的属性： skuPrice、skuImg、hasStock、hotScore,brandName、brandImg、catalogName、attrs
            */
            model.setSkuPrice(sku.getPrice());
            model.setSkuImg(sku.getSkuDefaultImg());


            //热度评分？
            model.setHotScore(0L);
            //查询品牌和分类的名字信息
            model.setBrandName(brandService.getById(sku.getBrandId()).getName());
            model.setBrandImg(brandService.getById(sku.getBrandId()).getLogo());
            model.setCatelogName(categoryService.getById(sku.getCatalogId()).getName());
            // 设置检索属性
            model.setAttrs(attrsList);
            if(map.size() == 0){
                model.setHasStock(true);
            }else{
                model.setHasStock(map.get(sku.getSkuId()));
            }
            skuEsModels.add(model);
        });

        // 数据发送给ES进行保存；
        R r = searchFeign.productStatusUp(skuEsModels);
        if((Integer) r.get("code") == 0){
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            //TODO: 重复调用？
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {

        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        SpuInfoEntity spuInfoEntity = getById(spuId);
        return spuInfoEntity;
    }
}