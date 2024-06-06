package com.abidbe.storyapp.story

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.abidbe.storyapp.api.ApiClient
import com.abidbe.storyapp.auth.UserPreferences
import com.abidbe.storyapp.auth.dataStore
import com.abidbe.storyapp.databinding.ActivityDetailStoryBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var viewModel: DetailStoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra("STORY_ID") ?: return

        var token: String? = null
        lifecycleScope.launch {
            token = UserPreferences.getInstance(dataStore).tokenFlow.firstOrNull()
            if (token != null) {
                initializeViewModel(token!!)
            }
        }

        viewModel.story.observe(this) { story ->
            binding.tvDetailName.text = story.name
            binding.tvDetailDescription.text = story.description
            Glide.with(this).load(story.photoUrl).into(binding.ivDetailPhoto)
            binding.tvLatitude.text = story.lat.toString()
            binding.tvLongitude.text = story.lon.toString()

        }

        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            viewModel.fetchStoryDetail(storyId)
        }
    }

    private fun initializeViewModel(token: String) {
        val apiService = ApiClient.getApiService(token)
        val repository = StoryRepository.getInstance(apiService)

        viewModel = ViewModelProvider(this, DetailStoryViewModelFactory(repository))
            .get(DetailStoryViewModel::class.java)
    }
}