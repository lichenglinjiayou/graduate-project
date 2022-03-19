package com.lichenglin.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@EnableAsync
@EnableScheduling
@Component
@Slf4j
public class CronScheduled {

    /**
     *  1. springboot中默认整合的定时任务cron表达式只能包含6位；
     *  2. 周一 -> 周日： 1 -> 7；
     *  3. 理想情况下，定时任务不应该阻塞，但是默认是阻塞的，当任务没有执行完，时间到，也不会执行下一个定时任务；
     *      1）.可以让业务通过异步的方式运行；
     *      2）.Springboot支持定时任务线程池；
     *      3). 定时任务异步执行；异步任务=> @EnableAsync-开启异步任务功能 @Async - 希望异步执行的方法标注该注解
     *      定时任务自动配置类；TaskSchedulingAutoConfiguration / 异步任务自动配置类： TaskExecutionAutoConfiguration]
     *
     *      异步+定时任务 => 解决定时任务不阻塞
     */
//    @Async
//    @Scheduled(cron = "*/2 * * * * 5")
//    public void hello() throws InterruptedException {
//      log.info("hello......");
//      TimeUnit.SECONDS.sleep(4);
//    }

}
