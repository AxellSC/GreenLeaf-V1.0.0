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

        binding.btnLogin.setOnClickListener {
            // Leer valores
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()

            // Validación local
            var hasError = false
            if (email.isEmpty()) {
                binding.tilEmail.error = "Correo obligatorio"
                hasError = true
            } else {
                binding.tilEmail.error = null
            }
            if (pass.isEmpty()) {
                binding.tilPassword.error = "Contraseña obligatoria"
                hasError = true
            } else {
                binding.tilPassword.error = null
            }
            if (hasError) return@setOnClickListener

            // Llamar al ViewModel
            viewModel.login(email, pass)
        }

        binding.btnRegisterAdmin.setOnClickListener {
            startActivity(Intent(this, RegistroAdminActivity::class.java))
        }

        binding.tvRegisterClient.setOnClickListener {
            startActivity(Intent(this, RegistroClientesActivity::class.java))
        }

        viewModel.loginResult.observe(this) { result ->
            if (!result.isSuccess) {
                // Mostrar el error devuelto por el ViewModel bajo contraseña
                binding.tilPassword.error = result.errorMessage
                return@observe
            }

            // Limpiar errores
            binding.tilPassword.error = null

            when (result.userType) {
                UserType.ADMIN -> {
                    val intent = Intent(this, FormActivity::class.java)
                    intent.putExtra("PROFILE", result.profileData as java.io.Serializable)
                    startActivity(intent)
                    finish()
                }
                UserType.CLIENTE -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish()
                }
                else -> {
                    binding.tilPassword.error = "Tipo de usuario desconocido"
                }
            }
        }
    }
}
