package com.cygnus.qwy_asnmt_kotlin.API

import com.cygnus.qwy_asnmt_kotlin.Model.MyPlaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by Cygnus on 05-07-2020.
 */

interface GoogleAPIService
{
    @GET
    fun getNearpyPlaces(@Url url:String):Call<MyPlaces>
}
