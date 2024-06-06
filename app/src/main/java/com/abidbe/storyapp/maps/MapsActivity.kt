package com.abidbe.storyapp.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.abidbe.storyapp.R
import com.abidbe.storyapp.api.ApiClient
import com.abidbe.storyapp.auth.UserPreferences
import com.abidbe.storyapp.auth.dataStore

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.abidbe.storyapp.databinding.ActivityMapsBinding
import com.abidbe.storyapp.story.StoryRepository
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsViewModel
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userPreferences = UserPreferences.getInstance(this.dataStore)

        lifecycleScope.launch {
            val token = userPreferences.tokenFlow.firstOrNull()
            if (token != null) {
                val apiService = ApiClient.getApiService(token)
                val repository = StoryRepository.getInstance(apiService)
                val factory = MapsViewModelFactory(repository)
                viewModel = ViewModelProvider(this@MapsActivity, factory)[MapsViewModel::class.java]
                viewModel.fetchStoriesWithLocation()

                viewModel.stories.observe(this@MapsActivity) { stories ->
                    if (stories.isNotEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        stories.forEach { story ->
                            story.lat?.let { lat ->
                                story.lon?.let { lon ->
                                    val latLng = LatLng(lat, lon)
                                    mMap.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .title(story.name)
                                            .snippet(story.description)
                                    )
                                    boundsBuilder.include(latLng)
                                }
                            }
                        }
                        val bounds = boundsBuilder.build()
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    } else {
                        Toast.makeText(this@MapsActivity, "No stories with location found", Toast.LENGTH_SHORT).show()
                    }
                }

                viewModel.loading.observe(this@MapsActivity) { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }

                viewModel.error.observe(this@MapsActivity) { errorMessage ->
                    errorMessage?.let {
                        Toast.makeText(this@MapsActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set a custom map style (optional)
        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        mMap.setMapStyle(style)
    }
}