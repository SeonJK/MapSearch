package com.example.mymap.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * MyMap
 * Created by SeonJK
 * Date: 2022-05-23
 * Time: 오후 5:10
 * */
@Parcelize
data class Place(
    @SerializedName("place_name")
    val buildingName: String,
    @SerializedName("address_name")
    val address: String,
    @SerializedName("road_address_name")
    val roadAddress: String,
    @SerializedName("x")
    val longitude: Double,
    @SerializedName("y")
    val latitude: Double
): Parcelable