package com.lichenglin.gulimall.search.service;

import com.lichenglin.gulimall.search.vo.SearchParam;
import com.lichenglin.gulimall.search.vo.SearchReturn;

public interface MallSearchService {

    /*
    根据检索参数，返回检索结果；
     */
    SearchReturn search(SearchParam searchParam);
}
