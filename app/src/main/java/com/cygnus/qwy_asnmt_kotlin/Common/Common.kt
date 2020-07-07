package com.cygnus.qwy_asnmt_kotlin.Common

import com.cygnus.qwy_asnmt_kotlin.API.GoogleAPIService
import com.cygnus.qwy_asnmt_kotlin.API.RetrofitClient
import com.cygnus.qwy_asnmt_kotlin.Model.Results

/**
 * Created by Cygnus on 06-07-2020.
 */
object Common {

    private val GOOGLE_API_URL = "https://maps.googleapis.com/"
    var currentResult:Results?=null

    val gooleApiService: GoogleAPIService
    get() = RetrofitClient.getClient(GOOGLE_API_URL).create(GoogleAPIService::class.java)
}