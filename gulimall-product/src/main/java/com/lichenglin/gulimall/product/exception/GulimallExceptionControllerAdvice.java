package com.lichenglin.gulimall.product.exception;

import com.lichenglin.common.exception.BizCodeEnum;
import com.lichenglin.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
   1 集中处理所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = {"com.lichenglin.gulimall.product"})
public class GulimallExceptionControllerAdvice {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerException(MethodArgumentNotValidException ex){
        log.error("数据校验出现异常",ex.getMessage(),ex.getClass());
        Map<String,String> errorMap = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getFieldErrors().forEach((item)->{
            String message = item.getDefaultMessage();
            String field = item.getField();
            errorMap.put(field,message);
        });
        return R.error(BizCodeEnum.VALIDATE_EXCEPTION.getCode(),BizCodeEnum.VALIDATE_EXCEPTION.getMessage()).put("data",errorMap);
    }


    @ExceptionHandler(value = Throwable.class)
    public R hadnlerException(Throwable throwable){
        return  R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
    }
}
