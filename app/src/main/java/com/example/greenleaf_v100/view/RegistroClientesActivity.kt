package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityRegistroClientesBinding
import com.example.greenleaf_v100.viewmodel.RegistroClientesViewModel
import java.util.regex.Pattern

class RegistroClientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroClientesBinding
    private val viewModel: RegistroClientesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installValidation()
        setupListeners()
        observeViewModel()
    }

    private fun installValidation() {
        // 1) Filtro sólo letras (incluye tildes y ñ) y espacios
        val namePattern = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")
        val nameFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isNullOrEmpty()) null
            else if (namePattern.matcher(source).matches()) source
            else ""
        }
        binding.etFirstName.filters       = arrayOf(nameFilter)
        binding.etLastNamePat.filters     = arrayOf(nameFilter)
        binding.etLastNameMat.filters     = arrayOf(nameFilter)

        // 2) Watcher genérico para nombre/apellidos
        val nameWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val txt = s?.toString().orEmpty()
                when (currentFocus?.id) {
                    binding.etFirstName.id -> {
                        if (txt.isBlank()) binding.tilFirstName.error = "Requerido"
                        else binding.tilFirstName.error = null
                    }
                    binding.etLastNamePat.id -> {
                        if (txt.isBlank()) binding.tilLastNamePat.error = "Requerido"
                        else binding.tilLastNamePat.error = null
                    }
                    binding.etLastNameMat.id -> {
                        if (txt.isBlank()) binding.tilLastNameMat.error = "Requerido"
                        else binding.tilLastNameMat.error = null
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        }
        binding.etFirstName.addTextChangedListener(nameWatcher)
        binding.etLastNamePat.addTextChangedListener(nameWatcher)
        binding.etLastNameMat.addTextChangedListener(nameWatcher)

        // 3) Email real-time
        binding.etEmailReg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isEmpty()) {
                    binding.tilEmailReg.error = "Requerido"
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    || !email.endsWith(".com", ignoreCase = true)
                ) {
                    binding.tilEmailReg.error = "Correo inválido (ej: usuario@dominio.com)"
                } else {
                    binding.tilEmailReg.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        })

        // 4) Password real-time
        val pwdPattern = Pattern.compile("^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}\$")
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = s.toString()
                when {
                    pwd.isEmpty() -> binding.tilPassword.error = "Requerido"
                    !pwdPattern.matcher(pwd).matches() ->
                        binding.tilPassword.error = "8+ chars, 1 número y 1 especial"
                    else -> binding.tilPassword.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        })

        // 5) Confirmar password real-time
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirm = s?.toString().orEmpty()
                val original = binding.etPassword.text.toString()
                when {
                    confirm.isEmpty() -> binding.tilConfirmPassword.error = "Requerido"
                    confirm != original -> binding.tilConfirmPassword.error = "No coincide"
                    else -> binding.tilConfirmPassword.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        })
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            // si hay algún error, no seguimos
            val hasError = listOf(
                binding.tilFirstName,
                binding.tilLastNamePat,
                binding.tilLastNameMat,
                binding.tilEmailReg,
                binding.tilPassword,
                binding.tilConfirmPassword
            ).any { it.error != null }

            if (hasError) return@setOnClickListener

            val fn    = binding.etFirstName.text.toString().trim()
            val lp    = binding.etLastNamePat.text.toString().trim()
            val lm    = binding.etLastNameMat.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val pw    = binding.etPassword.text.toString().trim()
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
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                binding.tilPassword.error = result.errorMessage
            }
        }
    }
}
