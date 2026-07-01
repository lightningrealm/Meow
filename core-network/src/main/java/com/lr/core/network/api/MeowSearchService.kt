package com.lr.core.network.api

import com.lr.core.network.model.CloudSearchResponse
import com.lr.core.network.model.HotSearchDetailResponse
import com.lr.core.network.model.SearchSuggestResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeowSearchService {

    @GET("/search/hot/detail")
    suspend fun getHotSearches(): HotSearchDetailResponse

    @GET("/search/suggest")
    suspend fun getSearchSuggest(
        @Query("keywords") keywords: String,
        @Query("type") type: String = "mobile"
    ): SearchSuggestResponse

    @GET("/cloudsearch")
    suspend fun search(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 1,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 30
    ): CloudSearchResponse
}
