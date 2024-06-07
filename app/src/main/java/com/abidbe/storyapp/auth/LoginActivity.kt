package com.abidbe.storyapp.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.abidbe.storyapp.story.MainActivity
import com.abidbe.storyapp.R
import com.abidbe.storyapp.api.ApiClient
import com.abidbe.storyapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: AuthViewModel
    private lateinit var userPreferences: UserPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        playAnimation()

        userPreferences = UserPreferences.getInstance(this.dataStore)

        val repository = AuthRepository(ApiClient.apiService)
        val viewModelFactory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()
            binding.progressBar.visibility = View.VISIBLE
            viewModel.login(email, password)
        }

        viewModel.loginResult.observe(this) { response ->
            binding.progressBar.visibility = View.GONE
            if (!response.error) {
                lifecycleScope.launch {
                    userPreferences.saveToken(response.loginResult.token)
                }
                Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            }


        }

        binding.btnRgs.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun playAnimation() {
        val logo = ObjectAnimator.ofFloat(binding.logo, View.ALPHA, 1f).setDuration(300)
        val appName = ObjectAnimator.ofFloat(binding.appname, View.ALPHA, 1f).setDuration(300)
        val tvLogin = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(300)
        val edEmail = ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 1f).setDuration(300)
        val edPass =
            ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 1f).setDuration(300)
        val btnLg = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(300)
        val or = ObjectAnimator.ofFloat(binding.or, View.ALPHA, 1f).setDuration(300)
        val btnRgs = ObjectAnimator.ofFloat(binding.btnRgs, View.ALPHA, 1f).setDuration(300)

        val together = AnimatorSet().apply {
            playTogether(logo, appName)
        }
        AnimatorSet().apply {
            playSequentially(together, tvLogin, edEmail, edPass, btnLg, or, btnRgs)
            start()
        }
    }
}