package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityRegistroAdminBinding
import com.example.greenleaf_v100.viewmodel.RegistroAdminViewModel

class RegistroAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroAdminBinding
    private val viewModel: RegistroAdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registro...
        binding.btnRegisterAdmin.setOnClickListener {
            val fn    = binding.etFirstNameAdmin.text.toString().trim()
            val lp    = binding.etLastNamePatAdmin.text.toString().trim()
            val lm    = binding.etLastNameMatAdmin.text.toString().trim()
            val email = binding.etEmailAdmin.text.toString().trim()
            val pw    = binding.etPasswordAdmin.text.toString().trim()
            val pw2   = binding.etConfirmPasswordAdmin.text.toString().trim()

            if (pw != pw2) {
                binding.etConfirmPasswordAdmin.error = "No coincide"
                return@setOnClickListener
            }
            viewModel.registerAdmin(fn, lp, lm, email, pw)
        }

        // Volver al login
        binding.btnBackToLoginAdmin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Observa resultado...
        viewModel.registroResult.observe(this) { result ->
            if (result.isSuccess) {
                // después de crear admin, volvemos al login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                binding.etPasswordAdmin.error = result.errorMessage
            }
        }
    }
}
