package com.abidbe.storyapp.story

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.abidbe.storyapp.R
import com.abidbe.storyapp.api.ApiClient
import com.abidbe.storyapp.auth.LoginActivity
import com.abidbe.storyapp.auth.UserPreferences
import com.abidbe.storyapp.auth.dataStore
import com.abidbe.storyapp.databinding.ActivityMainBinding
import com.abidbe.storyapp.maps.MapsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: StoryViewModel
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences.getInstance(this.dataStore)
        launchLogin()

        setContentView(binding.root)

        storyAdapter = StoryAdapter().apply {
            addLoadStateListener { loadState ->
                binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            }
        }

        binding.rvStories.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storyAdapter.retry() }
        )

        binding.actionLogout.setOnClickListener(this)
        binding.actionAddStory.setOnClickListener(this)
        binding.changeLanguage.setOnClickListener(this)
        binding.actionMaps.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
        tokenAvailable()
    }

    private fun launchLogin() {
        lifecycleScope.launch {
            userPreferences.tokenFlow.collect { token ->
                if (token == null) {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun tokenAvailable() {
        lifecycleScope.launchWhenStarted {
            userPreferences.tokenFlow.collect { token ->
                token?.let {
                    initializeViewModel(it)
                }
            }
        }
    }

    private fun initializeViewModel(token: String) {
        val apiService = ApiClient.getApiService(token)
        val repository = StoryRepository.getInstance(apiService)

        viewModel = ViewModelProvider(this, StoryViewModelFactory(repository))
            .get(StoryViewModel::class.java)

        viewModel.storyList.observe(this, { stories ->
            storyAdapter.submitData(lifecycle, stories)
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.actionLogout -> {
                lifecycleScope.launch {
                    userPreferences.clearToken()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.logout_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            }

            binding.actionAddStory -> {
                startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
            }

            binding.changeLanguage -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        super.onBackPressed()
    }
}