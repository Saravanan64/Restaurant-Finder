package com.cygnus.qwy_asnmt_kotlin.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Cygnus on 06-07-2020.
 */
object RetrofitClient {

    private var retrofit:Retrofit ?= null


    fun getClient(baseUrll:String):Retrofit
    {
        if(retrofit==null)
        {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrll)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

}