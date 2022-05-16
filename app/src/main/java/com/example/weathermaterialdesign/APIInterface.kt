package com.example.weathermaterialdesign

import retrofit2.Call
import retrofit2.http.*


interface APIInterface {

    @GET("/v1/current.json?aqi=no")
    fun getWeather(@Query("key") key : CharSequence, @Query("q") location : CharSequence): Call<Response?>?

}