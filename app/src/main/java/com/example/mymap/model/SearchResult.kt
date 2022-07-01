package com.example.mymap.model

import com.google.gson.annotations.SerializedName

/**
 * MyMap
 * Created by SeonJK
 * Date: 2022-05-23
 * Time: 오후 5:09
 * */
data class SearchResult (
    @SerializedName("meta") val meta: PlaceMeta,
    @SerializedName("documents") val place: List<Place>
)

data class PlaceMeta (
    @SerializedName("total_count")
    val totalCount: Int
    )