package com.lichenglin.gulimall.product.web;

import com.lichenglin.gulimall.product.entity.CategoryEntity;
import com.lichenglin.gulimall.product.service.CategoryService;
import com.lichenglin.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/","/index.html"})
    public String goToIndexPage(Model model){
        //TODO:查出所有一级分类
        List<CategoryEntity> entityList = categoryService.getAllLevelOneCategory();
        model.addAttribute("categorys",entityList);
        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String,List<Catalog2Vo>> getCatelogJson(){
        Map<String,List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     *  test distributed Lock - ReentrantLock
     *   1.The lock will be automatically renewed,
     *     and you don't need to worry about the lock automatically expired due to the long business time,
     *     and it will be deleted from the redis cache;
     *   2.The default timeout will be set for the lock, the default value is 30s；
     *   3. If you explicitly set a lock expiration time, the lock will no longer automatically renew;
     *   4. recommend explicitly set expiration time, avoiding automatically new;
     * @return
     */
    @RequestMapping("/hello")
    @ResponseBody
    public String re_Lock(){
        RLock r_lock = redissonClient.getLock("r_lock");
        r_lock.lock();
        try{
            Thread.sleep(5000);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            r_lock.unlock();
        }
        return "hello";
    }

    /**
     *  test distributed Lock - ReadWriteLock
     * @return
     */
    @RequestMapping("/write")
    @ResponseBody
    public String readWriteLock(){
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        RLock rLock = rwLock.writeLock();
        rLock.lock();
        String string = null;
        try {
            string = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set("write",string);
            Thread.sleep(8000);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return "success";
    }

    @ResponseBody
    @RequestMapping("read")
    public String readWriteLock2(){
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        RLock rLock = rwLock.readLock();
        rLock.lock();
        String string = null;
        try {
            string = stringRedisTemplate.opsForValue().get("write");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return string;
    }

    /**
     *  test distributed Lock - Semaphore
     *  Semaphore can be used to limit requests
     *  when semaphore is not enough, You can return to prompt information immediately;
     * @return
     */
    @RequestMapping("/park")
    @ResponseBody
    public String semaphore(){
        RSemaphore semaphore = redissonClient.getSemaphore("sLock");
        semaphore.tryAcquire();
        return "acquire";
    }

    @RequestMapping("/go")
    @ResponseBody
    public String semaphore2(){
        RSemaphore semaphore = redissonClient.getSemaphore("sLock");
        semaphore.release();
        return "release";
    }

    /**
     * test distributed Lock - CountDownLatch
     *  control thread synchronization
     * @return
     */
    @RequestMapping("/task")
    @ResponseBody
    public String finishTask(){
        RCountDownLatch cLock = redissonClient.getCountDownLatch("cLock");
        try {
            cLock.trySetCount(5);
            cLock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return "finished";
        }
    }

    @RequestMapping("/finish/{id}")
    @ResponseBody
    public String finish(@PathVariable("id")Long id){
        RCountDownLatch cLock = redissonClient.getCountDownLatch("cLock");
        cLock.countDown();
        return "task-"+id+"finished";
    }

}
