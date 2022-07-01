package com.example.mymap.api

import com.example.mymap.model.SearchResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * MyMap
 * Created by SeonJK
 * Date: 2022-05-30
 * Time: 오후 3:57
 * */
interface POIService {

    @GET("/v2/local/search/keyword.json")
    fun searchKeyword(
        @Header("Authorization") key: String,
        @Query("query") query: String
    ): Call<SearchResult>

}