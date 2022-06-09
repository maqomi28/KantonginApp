package com.project.kantongin.retrofit

import com.project.kantongin.model.Transaction
import retrofit2.Call
import retrofit2.http.GET

interface Api {
    @GET("kantongku/id")
    fun getTransaction(): Call<ArrayList<Transaction>>
}