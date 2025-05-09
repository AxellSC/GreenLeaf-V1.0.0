package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityLoginBinding
import com.example.greenleaf_v100.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()  // tu ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegistroAdminActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()
            viewModel.login(email, pass)
        }

        viewModel.loginResult.observe(this) { result ->
            if (result.isSuccess) {
                // Ya autenticado y perfil cargado
                viewModel.adminProfile.value?.let { admin ->
                    // Por ejemplo, enviar el admin a tu FormActivity
                    val intent = Intent(this, FormActivity::class.java).apply {
                        putExtra("ADMIN_PROFILE", admin)  // asegúrate de que ModelAdmin implemente Parcelable o Serializable
                    }
                    startActivity(intent)
                    finish()
                }
            } else {
                binding.etPassword.error = result.errorMessage
            }
        }
    }

}
