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

    // inject RedissonClient Object;
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
    // use @caching combine multiple operation
    // rectify data delete two cache
    @Caching(evict = {@CacheEvict(value = "category", key = "'getAllLevelOneCategory'"),
            @CacheEvict(value = "category", key = "'getCatalogJson'")})
    // @CacheEvict(value = "category",allEntries = true) =>  delete all data under the 'category' partition
    // @CachePut
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setCatelogId(category.getCatId());
            categoryBrandRelationEntity.setCatelogName(category.getName());
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * @return
     * @Cacheable: The return result of the current method needs to be cached,
     * If hit in the cache,the method will not be called;
     * value : Set the partition to which the cache is added
     * key : set generated key
     * <p>
     * Attention:
     * 1. key is automatically generated, cache name :: SimpleKey[]
     * 2. value is serializable outcome
     * 3. The default cache expiration time is never expire
     */
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getAllLevelOneCategory() {
        List<CategoryEntity> entities = this.list(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return entities;
    }

    @Cacheable(value = {"cateogory"}, key = "#root.methodName", sync = true)
    @Override
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
     * Problems caused by cache invalidation in a high concurrency environment:
     * 1. Cache penetration : not hit in cache and data don't exist in the DB,lots of requests
     * falls into DB. If it can't afford the pressure, DB Server will down. Solutions: cache null value in a period of time;
     * 2. Cache Avalanche : A large amount of cached data is invalidated at the same time,leading requests will access DB Server.
     * Solution: Add a random value to the original expiration time of the cached data to prevent the cached data from invalidating at the same time;
     * 3. Cache breakdown: hot data stored in Cache invalidate. Solution: Locking to ensure that only one request accesses the database at the same time;
     * @return
     */

    /**
     * Problem：How the data in the cache is consistent with the data in the database?
     * 1.double writing；rectify the data in the database and then update the data in the cache;
     * 2.invalidation mode；rectify the data in the database and then delete the data in the cache;
     *
     * @return
     */
    @Cacheable(value = "cateogory", key = "#root.methodName", sync = true)
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        // The name of the lock determines the granularity of the lock;
        // use distributed Lock - Reentrant Lock;
        RLock lock = redissonClient.getLock("catalogJSON-lock");
        lock.lock(30, TimeUnit.SECONDS);
        Map<String, List<Catalog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }

    /*
       Distributed Lock:
       SET Key value [EX seconds] [PX milliseconds] [NX]
       When the key does not exist in the cache, store the key-value pair;
       EX|PX - set expiration time;
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        /*
            Generate its own UUID for each server:
                Reason : The server executes business logic for too long, causing its own lock to expire long ago.
                When the business is finished deleting the lock,
                it will delete the lock resources occupied by other servers.
         */
        String token = UUID.randomUUID().toString();
        /*
           Preempting distributed locks;
           Set the expiration time for the key-value to prevent the distributed lock from being released due to server power failure and other reasons,
           and other servers cannot obtain the lock;
         */
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.SECONDS);
        if (lock == true) {
            // Set lock-token success;
            Map<String, List<Catalog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                /*
                    The process of deleting the lock should also be guaranteed to be an atomic operation, that is,
                    to determine whether the UUID is correct and to delete the cache record,
                    either succeed or fail at the same time.
                    Consequently, The code below is not entirely correct.

                    String lockValue = stringRedisTemplate.opsForValue().get("lock");
                    if(token.equals(lockValue)){
                    stringRedisTemplate.delete("lock");
                 */
                // lua script guarantee atomic opeartion;
                String script = "if redis call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // execute lua script;
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), token);
            }
            return dataFromDB;
        } else {
            // Set lock-token fail, sleep 2s, then spin retry, try to lock again.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        // Firstly, attempt to obtain data from redis;
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        // Judge whether the data queried from the Redis is empty;
        if (!StringUtils.isEmpty(catalogJSON)) {
            // Redis cache has data, return data directly;
            // Before returning data, convert JSON String to Map;
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        // Not hit in Cache, query Mysql;
        List<CategoryEntity> allData = this.baseMapper.selectList(null);
        List<CategoryEntity> levelOneCategory = getParent_cid(allData, 0L);
        Map<String, List<Catalog2Vo>> map = new HashMap<>();
        levelOneCategory.forEach((item) -> {
            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
            List<CategoryEntity> entities = getParent_cid(allData, item.getCatId());
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
        // Because of the data type stored in the cache is string, we should call toJSONString() to convert Map to String;
        String s = JSON.toJSONString(map);
        // Cache JSON data into the redis;
        stringRedisTemplate.opsForValue().set("catalogJSON", s);
        return map;
    }

    /*
     monolithic application: lock the current object.
     Because of object conducted by Bean Container is singleton by default,so synchronized(this)
     can guarantee only one request accesses the DB.
     But in the distributed environment, a microservice will be distributed on multiple servers,
     so this way can only limit multiple requests in one server.
     Finally, this project determines to use  distributed lock.
    */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        synchronized (this) {
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