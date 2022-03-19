package com.lichenglin.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lichenglin.gulimall.product.entity.CategoryBrandRelationEntity;
import com.lichenglin.gulimall.product.service.CategoryBrandRelationService;
import com.lichenglin.gulimall.product.vo.Catalog2Vo;
import com.lichenglin.gulimall.product.vo.Catalog3Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.CategoryDao;
import com.lichenglin.gulimall.product.entity.CategoryEntity;
import com.lichenglin.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listByTree() {
        List<CategoryEntity> entities = categoryDao.selectList(null);
        List<CategoryEntity> levelOne = new ArrayList<>();
        for (CategoryEntity entity : entities) {
            if (entity.getParentCid() == 0) {
                //找出所有一级分类的商品，添加到list集合中；
                levelOne.add(entity);
            }
        }
        for (CategoryEntity entity : levelOne) {
            //遍历一级商品的集合，对每个一级商品都调用getChildrens()方法；
            getChildrens(entity, entities);
        }
        //对一级商品按照自定义的排序规则进行排序；
        Collections.sort(levelOne, new Comparator<CategoryEntity>() {
            @Override
            public int compare(CategoryEntity o1, CategoryEntity o2) {
                return (o1.getSort() == null ? 0 : o1.getSort()) -
                        (o2.getSort() == null ? 0 : o2.getSort());
            }
        });
        return levelOne;
    }


    public void getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> child = new ArrayList<>();
        for (CategoryEntity entity : all) {
            //递归1次：找到所有1级商品包含的2级商品；
            //递归2次：找到所有2级商品包含的3级商品；
            if (entity.getParentCid() == root.getCatId()) {
                //将符合要求的商品添加到list集合中；
                child.add(entity);
            }
        }
        Collections.sort(child, new Comparator<CategoryEntity>() {
            @Override
            public int compare(CategoryEntity o1, CategoryEntity o2) {
                return (o1.getSort() == null ? 0 : o1.getSort()) -
                        (o2.getSort() == null ? 0 : o2.getSort());
            }
        });
        //递归1次：设置1级商品的children属性；
        //递归2次：设置2级商品的children属性；
        root.setChildren(child);
        //只有当含有下一级的商品时，才进入下一次递归操作；
        if (child.size() != 0) {
            for (CategoryEntity entity : child) {
                getChildrens(entity, all);
            }
        }
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
//        TODO 检查待删除的菜单，是否在别处被引用
        categoryDao.deleteBatchIds(asList);
    }


    @Override
    public Long[] findCatelogIds(Long catelogId) {
        List<Long> catelogIds = new ArrayList<>();
        getAllParentPath(catelogId, catelogIds);
        Collections.reverse(catelogIds);
        return catelogIds.toArray(new Long[catelogIds.size()]);
    }

    @Override
    @Transactional
    //缓存-失效模式
//    @CacheEvict(value = "category",key = "'getAllLevelOneCategory'")
    //组合多操作
    @Caching(evict = {@CacheEvict(value = "category",key = "'getAllLevelOneCategory'"),
                      @CacheEvict(value = "category",key = "'getCatalogJson'")})
//    @CacheEvict(value = "category",allEntries = true)
    // 双写模式
//    @CachePut
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setCatelogId(category.getCatId());
            categoryBrandRelationEntity.setCatelogName(category.getName());
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    @Cacheable(value = {"category"},key = "#root.methodName",sync = true) // 代表当前方法的结果需要缓存，缓存中有，则不在调用方法；
    //每一个需要缓存的数据，都需要指定缓存的名字；
    //redis中缓存数据的key,默认为cache name::SimpleKey []
    //默认生成的缓存永不过期；
    //存放的数据为Java序列化后的数据；
    /*常规数据（读多写少，即时性，一致性要求不高的数据） = > SpringCache
      特殊数据 特殊设计
    * */
  @Override
    public List<CategoryEntity> getAllLevelOneCategory() {
        List<CategoryEntity> entities = this.list(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return entities;
    }

    @Cacheable(value = "cateogory",key = "#root.methodName",sync = true)
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        List<CategoryEntity> allData = this.baseMapper.selectList(null);
        List<CategoryEntity> levelOneCategory = getParent_cid(allData, 0L);
        Map<String, List<Catalog2Vo>> map = new HashMap<>();
        levelOneCategory.forEach((item) -> {
            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
            List<CategoryEntity> entities = getParent_cid(allData, item.getParentCid());
            if (entities != null) {
                entities.forEach((entity) -> {
                    List<Catalog3Vo> catalog3Vos = new ArrayList<>();
                    List<CategoryEntity> entities1 = getParent_cid(allData, entity.getParentCid());
                    if (entities1 != null) {
                        entities1.forEach((bean) -> {
                            Catalog3Vo catalog3Vo = new Catalog3Vo(entity.getCatId().toString(), bean.getCatId().toString(), bean.getName());
                            catalog3Vos.add(catalog3Vo);
                        });
                    }
                    Catalog2Vo catalog2Vo = new Catalog2Vo(item.getCatId().toString(), catalog3Vos, entity.getCatId().toString(), entity.getName());
                    catalog2Vos.add(catalog2Vo);
                });
            }
            map.put(item.getCatId().toString(), catalog2Vos);
        });
        String s = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJSON", s);
        return map;
    }

    /**
     * 堆外内存溢出异常？
     * springboot2.0以后默认使用lettuce作为操作redis的客户端，使用netty进行网络通信；
     * lettuce的bug导致堆外内存溢出。
     * 可以通过-Dio.netty.maxDirectMemory进行设置，但是不能只使用该设置去调大内存；
     * 解决方案： 1.升级lettuce客户端；2.切换使用jedis；
     * <p>
     * 1.空结果缓存：缓存穿透；
     * 2.过期时间加随机值：缓存雪崩；
     * 3.加锁：缓存击穿；
     * 本地锁，由于所有的中间件都是单实例的，所有可以锁住一个服务器上的并发请求；但是
     * 在多台服务器上，本地锁只能锁自己的服务器，因此需要使用分布式锁；
     *
     * @return
     */


    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        // 加入redis缓存
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        //先查看redis缓存中是否有该数据，没有则查数据库，并将查询结果加入到缓存中
        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();

            return catalogJsonFromDB;
        }
        //否则，直接获取缓存中的数据，反序列化后，进行返回；
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return result;
    }

    //使用Redisson分布式锁

    /**
     * 问题：缓存中的数据和数据库中的数据如何保持一致？
     * 1.双写；修改完数据库，同时接着修改缓存中的数据；
     * 2.失效；修改完数据库，删除缓存中的数据；
     * @return
     */
    @Override
    @Cacheable(value = "cateogory",key = "#root.methodName",sync = true)
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        /**
         * 1.一次访问数据库查询出所有的数据；
         */
        //锁的名字决定锁的粒度；约定：具体缓存某个数据，则锁在具体的商品上；
        RLock lock = redissonClient.getLock("catalogJSON-lock");
        lock.lock(30, TimeUnit.SECONDS);
        Map<String, List<Catalog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        }finally {
                lock.unlock();
            }
            return dataFromDB;
    }

    //使用分布式锁
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        /**
         * 1.一次访问数据库查询出所有的数据；
         */
        //搶占分佈式鎖
        String token = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.SECONDS);
        if (lock == true) {
            Map<String, List<Catalog2Vo>> dataFromDB;
            try {
                //设置锁的过期时间，防止死锁，但是需要和加锁做到原子操作
                //stringRedisTemplate.expire("lock",30, TimeUnit.SECONDS);
                dataFromDB = getDataFromDB();
            } finally {
//                String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if(token.equals(lockValue)){
//                stringRedisTemplate.delete("lock");
//            }
//            lua脚本解锁 = > 确保解锁的原子性；
                String script = "if redis call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), token);
            }
            return dataFromDB;
        } else {
            //获取锁失败，自旋重试
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        //获取锁后，先去缓存中判断，是否缓存中已经存在数据；
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        //如果没有，则查询数据库
//        System.out.println("查询数据库");
        List<CategoryEntity> allData = this.baseMapper.selectList(null);
        List<CategoryEntity> levelOneCategory = getParent_cid(allData, 0L);
        Map<String, List<Catalog2Vo>> map = new HashMap<>();
        levelOneCategory.forEach((item) -> {
            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
            List<CategoryEntity> entities = getParent_cid(allData,item.getCatId());
            if (entities != null) {
                entities.forEach((entity) -> {
                    List<Catalog3Vo> catalog3Vos = new ArrayList<>();
                    List<CategoryEntity> entities1 = getParent_cid(allData, entity.getCatId());
                    if (entities1 != null) {
                        entities1.forEach((bean) -> {
                            Catalog3Vo catalog3Vo = new Catalog3Vo(entity.getCatId().toString(), bean.getCatId().toString(), bean.getName());
                            catalog3Vos.add(catalog3Vo);
                        });
                    }
                    Catalog2Vo catalog2Vo = new Catalog2Vo(item.getCatId().toString(), catalog3Vos, entity.getCatId().toString(), entity.getName());
                    catalog2Vos.add(catalog2Vo);
                });
            }
            map.put(item.getCatId().toString(), catalog2Vos);
        });
        String s = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJSON", s);
        return map;
    }

    //使用本地锁
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        /**
         * 1.一次访问数据库查询出所有的数据；
         */
        synchronized (this) {
            //获取锁后，先去缓存中判断，是否缓存中已经存在数据；
            return getDataFromDB();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> allData, Long parent_cid) {
        List<CategoryEntity> collect = allData.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    /**
     * 递归三级目录的方法
     */
    private List<Long> getAllParentPath(Long catelogId, List<Long> list) {
        list.add(catelogId);
        CategoryEntity parent = this.getById(catelogId);
        if (parent.getParentCid() != 0) {
            getAllParentPath(parent.getParentCid(), list);
        }
        return list;
    }

}