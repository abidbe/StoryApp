package com.abidbe.storyapp.story

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.abidbe.storyapp.api.ApiService
import com.abidbe.storyapp.api.ListStoryItem
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

    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = { StoryPagingSource(apiService) }
        ).liveData
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
