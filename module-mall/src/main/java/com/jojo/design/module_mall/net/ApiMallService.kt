package com.jojo.design.module_mall.net

import com.jojo.design.module_mall.bean.CategoryBean
import com.jojo.design.module_mall.bean.FilterBean
import com.jojo.design.module_mall.bean.RecordsEntity
import com.smart.novel.net.BaseHttpResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


/**
 *    author : JOJO
 *    e-mail : 18510829974@163.com
 *    date   : 2018/12/29 3:42 PM
 *    desc   : 接口管理
 */
interface ApiMallService {

    //获取热门搜索
    @GET("search/recowords")
    fun getHotList(): Observable<BaseHttpResponse<List<String>>>

    //点击分类标签搜索商品  key=05bddc6fa2cc21c57ea1ae11de699bcd&outCategoryId=2&page=0&sort=0&t=1546073569203&tagid=0&version=2.3.04
    @GET("search/list")
    fun getSearchGoods(@Query("outCategoryId") outCategoryId: String, @Query("keyword") keyword: String, @Query("page") page: Int): Observable<BaseHttpResponse<RecordsEntity>>

    //点击筛选栏->选择分类  key=4de264770bd2568b938c832f96f345c1&outCategoryId=1&t=1547004222303&version=2.3.04
    @GET("search/list/filtrate/category")
    fun getCategoryList(@Query("outCategoryId") outCategoryId: String, @Query("keyword") keyword: String): Observable<BaseHttpResponse<List<CategoryBean>>>

    //点击筛选栏->右侧筛选  key=3dfe6888b9b99ce6648d0c4f59575e02&outCategoryId=1&t=1547004464740&version=2.3.04
    @GET("search/list/filtrate/others")
    fun getFilterData(@Query("outCategoryId") outCategoryId: String): Observable<BaseHttpResponse<FilterBean>>
}