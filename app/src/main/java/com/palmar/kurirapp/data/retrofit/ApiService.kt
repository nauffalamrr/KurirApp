package com.palmar.kurirapp.data.retrofit

import com.palmar.kurirapp.data.LoginRequest
import com.palmar.kurirapp.data.LoginResponse
import com.palmar.kurirapp.data.MessageResponse
import com.palmar.kurirapp.data.OptimizeRouteRequest
import com.palmar.kurirapp.data.OptimizeRouteResponse
import com.palmar.kurirapp.data.Task
import com.palmar.kurirapp.data.TaskResponse
import com.palmar.kurirapp.data.TripHistory
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/api/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("/api/route")
    fun optimizeRoute(@Body request: OptimizeRouteRequest): Call<OptimizeRouteResponse>

    @GET("/api/task")
    fun getAllTasks(): Call<List<Task>>

    @GET("/api/task/{id}")
    fun getTaskDetail(@Path("id") taskId: Int): Call<TaskResponse>

    @POST("/api/task/{id}/accept")
    fun acceptTask(@Path("id") taskId: Int): Call<MessageResponse>

    @POST("/api/task/{id}/complete")
    fun completeTask(@Path("id") taskId: Int): Call<MessageResponse>

    @GET("/api/history")
    fun getTripHistory(): Call<List<TripHistory>>

    @POST("/api/logout")
    fun logout(): Call<MessageResponse>
}