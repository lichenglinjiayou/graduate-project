package com.lichenglin.gulimall.product;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/** 1 后端校验：使用JSR303
 *   (1)为实体类添加校验注解；@NotNull @Email @Future ... javax.validation.constraints
 *   (2)告诉springMVC数据需要校验，在controller中使用到实体类的地方标注@Valid开启校验功能，只标注校验注解，是无效的；
 *   (3)自定义错误消息提示 @NotBlank(message = "xxx")
 *   (4)在controller校验数据的后边，紧跟BindingResult对象，该对象封装校验的结果，由此可以自定义返回的R JSON字符串；
 *
 *  1 统一异常处理
 * @ControllerAdvice
 *    (1)编写异常处理类@RestControllerAdvice，集中处理所有异常，类中每个方法@ExceptionHandler(value = xxx.class)
 *    (2)抽取出枚举类，进行规范的异常分类；
 *
 *  1 JSR303分组校验
 *      例如：新增时候，brandId自增，因此不需要校验；而修改时，则必须携带brandId；等等
 *      (1)通过校验注解标注，哪些情况需要进行校验；不标注分组校验注解的属性，不会进行任何校验
 *      (2)不标注分组的CRUD，所有的校验注解都会生效；
 *
 *  1. 自定义校验功能
 *      (1) 自己编写自定义的校验注解；
 *      (2) 编写自定义的校验器；
 *      (3) 关联自定义的校验器和自定义的校验注解；
 *      (4) 一个校验注解可以绑定多个自定义的校验器；
 *
 *
 * 1. EnableRedisHttpSession => RedisHttpSessionConfiguration
 *      RedisOperationsSessionRepository : session crud的封装类
 *      SessionRepositoryFilter ：session存储过滤器
 *      创建对象时，获取sessionRepository;
 *      使用wrapperRequest包装request;
 *      调用wrapperRequest的getSession方法，从sessionRepository找到session对象；
 *
 *   */
@EnableCaching
@EnableFeignClients(basePackages = "com.lichenglin.gulimall.product.feign")
@SpringBootApplication
@MapperScan(value = {"com.lichenglin.gulimall.product.dao"})
@EnableDiscoveryClient
@EnableRedisHttpSession
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
