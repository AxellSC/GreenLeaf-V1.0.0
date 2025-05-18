package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityLoginBinding
import com.example.greenleaf_v100.viewmodel.LoginViewModel
import com.example.greenleaf_v100.viewmodel.UserType

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón “Entrar”
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()
            viewModel.login(email, pass)
        }

        // Botón “Registrar administrador”
        binding.btnRegisterAdmin.setOnClickListener {
            startActivity(Intent(this, RegistroAdminActivity::class.java))
        }

        // Texto “Regístrate como cliente”
        binding.tvRegisterClient.setOnClickListener {
            startActivity(Intent(this, RegistroClientesActivity::class.java))
        }

        // Observa resultado de login
        viewModel.loginResult.observe(this) { result ->
            if (!result.isSuccess) {
                binding.etPassword.error = result.errorMessage
                return@observe
            }

            when (result.userType) {
                UserType.ADMIN -> {
                    // Enviar a FormActivity (admin)
                    val intent = Intent(this, FormActivity::class.java)
                    intent.putExtra("PROFILE", result.profileData as java.io.Serializable)
                    startActivity(intent)
                    finish()
                }
                UserType.CLIENTE -> {
                    // Enviar a catálogo (cliente)
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish()
                }
                else -> {
                    binding.etPassword.error = "Tipo de usuario desconocido"
                }
            }
        }
    }
}
