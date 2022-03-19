package com.lichenglin.gulimall.search.web;

import com.lichenglin.gulimall.search.service.MallSearchService;
import com.lichenglin.gulimall.search.vo.SearchParam;
import com.lichenglin.gulimall.search.vo.SearchReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @RequestMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);
        SearchReturn result =  mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
