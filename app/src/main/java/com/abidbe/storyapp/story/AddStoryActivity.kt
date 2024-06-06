package com.abidbe.storyapp.story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.abidbe.storyapp.R
import com.abidbe.storyapp.api.ApiClient
import com.abidbe.storyapp.auth.UserPreferences
import com.abidbe.storyapp.auth.dataStore
import com.abidbe.storyapp.databinding.ActivityAddStoryBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.abidbe.storyapp.getImageUri
import com.abidbe.storyapp.reduceFileImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var viewModel: AddStoryViewModel
    private lateinit var userPreferences: UserPreferences
    private var selectedImageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG)
                    .show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        userPreferences = UserPreferences.getInstance(this.dataStore)
        setupViewModel()

        binding.switchUseCurrentLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    getCurrentLocation()
                }
            } else {
                currentLocation = null
            }
        }

        binding.buttonChoosePhoto.setOnClickListener { startGallery() }
        binding.buttonTakePhoto.setOnClickListener { startCamera() }
        binding.buttonAdd.setOnClickListener { uploadStory() }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = it
                    }
                }
        }
    }


    private fun setupViewModel() {
        userPreferences.tokenFlow.asLiveData().observe(this) { token ->
            if (token != null) {
                val apiService = ApiClient.getApiService(token)
                val repository = StoryRepository.getInstance(apiService)
                viewModel = ViewModelProvider(this, AddStoryViewModelFactory(repository))
                    .get(AddStoryViewModel::class.java)
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            showImage()

        } else {
            Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        selectedImageUri = getImageUri(this)
        launcherIntentCamera.launch(selectedImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            Toast.makeText(this, getString(R.string.failed_picture), Toast.LENGTH_SHORT).show()
        }
    }


    private fun showImage() {
        selectedImageUri?.let {
            Log.d("AddStory", "showImage: $it")
            binding.imagePreview.setImageURI(it)
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString()
        if (selectedImageUri != null && description.isNotBlank()) {
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val file = File(cacheDir, "temp_image.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val compressedFile = file.reduceFileImage()

            val requestBody = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody =
                MultipartBody.Part.createFormData("photo", compressedFile.name, requestBody)
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

            val latBody = currentLocation?.latitude?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lonBody = currentLocation?.longitude?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())

            viewModel.uploadStory(descriptionBody, multipartBody, latBody, lonBody)
                .observe(this) { response ->
                    if (response.error == false) {
                        Toast.makeText(
                            this,
                            getString(R.string.story_added_successfully), Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(
                this,
                getString(R.string.please_select),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}