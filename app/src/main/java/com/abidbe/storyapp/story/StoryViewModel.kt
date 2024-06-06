package com.abidbe.storyapp.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.abidbe.storyapp.api.ListStoryItem

class StoryViewModel(private val repository: StoryRepository) : ViewModel() {
    val storyList: LiveData<PagingData<ListStoryItem>> =
        repository.getStories().cachedIn(viewModelScope)
}