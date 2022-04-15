package com.lichenglin.gulimall.search.service;

import com.lichenglin.gulimall.search.vo.SearchParam;
import com.lichenglin.gulimall.search.vo.SearchReturn;

public interface MallSearchService {
    /*
        MallSearchService interface define method to receive search condition and return query data;
    */
    SearchReturn search(SearchParam searchParam);
}
