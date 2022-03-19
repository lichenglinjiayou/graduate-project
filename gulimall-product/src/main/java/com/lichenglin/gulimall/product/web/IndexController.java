package com.lichenglin.gulimall.product.web;

import com.lichenglin.gulimall.product.entity.CategoryEntity;
import com.lichenglin.gulimall.product.service.CategoryService;
import com.lichenglin.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;
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
        Map<String,List<Catalog2Vo>> map = categoryService.getCatalogJsonFromDBWithRedissonLock();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
}
