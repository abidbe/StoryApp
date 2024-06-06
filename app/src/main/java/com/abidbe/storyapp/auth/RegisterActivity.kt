package com.abidbe.storyapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abidbe.storyapp.R
import com.abidbe.storyapp.api.ApiClient
import com.abidbe.storyapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private lateinit var viewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        val repository = AuthRepository(ApiClient.apiService)
        val viewModelFactory = AuthViewModelFactory(repository)
        viewModel = viewModelFactory.create(AuthViewModel::class.java)

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            binding.progressBar.visibility = View.VISIBLE
            viewModel.register(name, email, password)
        }

        viewModel.registerResult.observe(this) { response ->
            binding.progressBar.visibility = View.GONE
            if (response != null) {
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(
                    this,
                    getString(R.string.registration_successful),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, getString(R.string.registration_failed), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnLgn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }
}