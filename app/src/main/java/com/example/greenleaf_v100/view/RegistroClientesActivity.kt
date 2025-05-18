package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityRegistroClientesBinding
import com.example.greenleaf_v100.viewmodel.RegistroClientesViewModel
import com.example.greenleaf_v100.view.LoginActivity

class RegistroClientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroClientesBinding
    private val viewModel: RegistroClientesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val fn    = binding.etFirstName.text.toString().trim()
            val lp    = binding.etLastNamePat.text.toString().trim()
            val lm    = binding.etLastNameMat.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val pw    = binding.etPassword.text.toString().trim()
            val pw2   = binding.etConfirmPassword.text.toString().trim()

            if (fn.isEmpty() || lp.isEmpty() || email.isEmpty() || pw.isEmpty()) {
                // Puedes agregar validaciones adicionales
                return@setOnClickListener
            }

            if (pw != pw2) {
                binding.etConfirmPassword.error = "No coincide"
                return@setOnClickListener
            }

            viewModel.registerCliente(fn, lp, lm, email, pw)
        }

        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.registroResult.observe(this) { result ->
            if (result.isSuccess) {
                // Registro exitoso → volver a login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                // Mostrar error
                binding.etPassword.error = result.errorMessage
            }
        }
    }
}