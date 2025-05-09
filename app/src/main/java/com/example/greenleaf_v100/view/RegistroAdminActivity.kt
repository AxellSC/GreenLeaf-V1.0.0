package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityRegistroAdminBinding
import com.example.greenleaf_v100.viewmodel.RegistroAdminViewModel
import kotlin.reflect.KProperty

class RegistroAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroAdminBinding
    private val viewModel: RegistroAdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnRegister.setOnClickListener {
            val fn    = binding.etFirstName.text.toString().trim()
            val lp    = binding.etLastNamePat.text.toString().trim()
            val lm    = binding.etLastNameMat.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val pw    = binding.etPassword.text.toString().trim()
            val pw2   = binding.etConfirmPassword.text.toString().trim()

            if (pw != pw2) {
                binding.etConfirmPassword.error = "No coincide"
                return@setOnClickListener
            }

            viewModel.register(fn, lp, lm, email, pw)
        }

        viewModel.registroResult.observe(this) { result ->
            if (result.isSuccess) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                binding.etPassword.error = result.errorMessage
            }
        }
    }
}


