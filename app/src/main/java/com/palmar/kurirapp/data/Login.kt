package com.palmar.kurirapp.data

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    val username: String,
    val password: String
)
