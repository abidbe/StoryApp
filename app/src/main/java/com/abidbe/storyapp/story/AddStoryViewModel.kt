package com.abidbe.storyapp.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abidbe.storyapp.api.UploadResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {

    fun uploadStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): LiveData<UploadResponse> {
        val result = MutableLiveData<UploadResponse>()
        viewModelScope.launch {
            val response = repository.uploadStory(description, photo, lat, lon)
            if (response.isSuccessful) {
                response.body()?.let {
                    result.postValue(it)
                }
            } else {
                result.postValue(UploadResponse(error = true, message = "Failed to upload story"))
            }
        }
        return result
    }
}

class AddStoryViewModelFactory(private val repository: StoryRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddStoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddStoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}