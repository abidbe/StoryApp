package com.abidbe.storyapp.auth

import com.abidbe.storyapp.api.ApiService
import com.abidbe.storyapp.api.LoginResponse
import com.abidbe.storyapp.api.RegisterResponse
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Response<RegisterResponse> {
        return apiService.registerUser(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }
}