package com.example.weathermaterialdesign
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


abstract class ApiClient {

    companion object{
        @Volatile
        private var INSTANCE: APIInterface? = null
        val API_KEY = "ADD YOUR API KEY HERE"
        fun getInstance(): APIInterface {
            synchronized(this) {
                val instance: APIInterface
                if (INSTANCE == null) {
                    instance = Retrofit.Builder()
                        .baseUrl("https://api.weatherapi.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(APIInterface::class.java)
                    INSTANCE = instance
                }
                else
                    instance = INSTANCE as APIInterface
                return instance
            }
        }
    }
}
