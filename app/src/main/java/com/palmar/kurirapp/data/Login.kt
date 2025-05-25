package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginRequest(
    val username: String,
    val password: String
) : Parcelable

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val password: String
) : Parcelable

@Parcelize
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user: User
) : Parcelable
