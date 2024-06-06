package com.abidbe.storyapp.story

import com.abidbe.storyapp.api.ApiService
import com.abidbe.storyapp.api.StoryResponse
import com.abidbe.storyapp.api.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class StoryRepository private constructor(private val apiService: ApiService) {

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService).also { instance = it }
            }
    }

    suspend fun getStories(): Response<StoryResponse> {
        return apiService.getStories()
    }

    suspend fun getStoryDetail(storyId: String) = apiService.getStoryDetail(storyId)

    suspend fun uploadStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): Response<UploadResponse> {
        return if (lat != null && lon != null) {
            apiService.uploadStoryWithLocation(description, photo, lat, lon)
        } else {
            apiService.uploadStory(description, photo)
        }
    }
    suspend fun getStoriesWithLocation(location: Int): Response<StoryResponse> {
        return apiService.getStoriesWithLocation(location)
    }
}
