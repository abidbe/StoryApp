package com.abidbe.storyapp.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class StoryResponse(

    @field:SerializedName("listStory")
    val listStory: List<ListStoryItem?> = emptyList(),

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class RegisterResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String
)

data class LoginResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("loginResult") val loginResult: LoginResult
)

data class LoginResult(
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("token") val token: String
)

data class ErrorResponse(
    @SerializedName("error") val error: Boolean?,
    @SerializedName("message") val message: String?
)

@Parcelize
data class ListStoryItem(

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,


    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("lat")
    val lat: Double? = null,

    @field:SerializedName("lon")
    val lon: Double? = null


) : Parcelable

data class StoryDetailResponse(
    val story: ListStoryItem
)

data class UploadResponse(
    @SerializedName("error") val error: Boolean?,
    @SerializedName("message") val message: String?
)