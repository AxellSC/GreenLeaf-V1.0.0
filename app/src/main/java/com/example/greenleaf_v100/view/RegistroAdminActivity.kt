package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityRegistroAdminBinding
import com.example.greenleaf_v100.viewmodel.RegistroAdminViewModel
import java.util.regex.Pattern

class RegistroAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroAdminBinding
    private val viewModel: RegistroAdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupValidation()
        setupListeners()
        observeViewModel()
    }

    private fun setupValidation() {
        // 1) Filtro sólo letras (con acentos y espacios)
        val namePattern = Pattern.compile("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")
        val nameFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isNullOrEmpty()) null
            else if (namePattern.matcher(source).matches()) source
            else ""
        }
        binding.etFirstNameAdmin.filters   = arrayOf(nameFilter)
        binding.etLastNamePatAdmin.filters = arrayOf(nameFilter)
        binding.etLastNameMatAdmin.filters = arrayOf(nameFilter)

        // 2) TextWatcher para nombres
        val nameWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                when (currentFocus?.id) {
                    binding.etFirstNameAdmin.id -> {
                        binding.tilFirstNameAdmin.error =
                            if (text.isEmpty()) "Campo obligatorio" else null
                    }
                    binding.etLastNamePatAdmin.id -> {
                        binding.tilLastNamePatAdmin.error =
                            if (text.isEmpty()) "Campo obligatorio" else null
                    }
                    binding.etLastNameMatAdmin.id -> {
                        binding.tilLastNameMatAdmin.error =
                            if (text.isEmpty()) "Campo obligatorio" else null
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        }
        binding.etFirstNameAdmin.addTextChangedListener(nameWatcher)
        binding.etLastNamePatAdmin.addTextChangedListener(nameWatcher)
        binding.etLastNameMatAdmin.addTextChangedListener(nameWatcher)

        // 3) Email en tiempo real
        binding.etEmailAdmin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                binding.tilEmailAdmin.error = when {
                    email.isEmpty() ->
                        "Campo obligatorio"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            || !email.endsWith(".com", true) ->
                        "Correo inválido (ej: usuario@dominio.com)"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        })

        // 4) Contraseña en tiempo real
        val pwdPattern = Pattern.compile("^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}\$")
        binding.etPasswordAdmin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = s.toString()
                binding.tilPasswordAdmin.error = when {
                    pwd.isEmpty() ->
                        "Campo obligatorio"
                    !pwdPattern.matcher(pwd).matches() ->
                        "Mínimo 8 caracteres, 1 número y 1 caracter especial"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        })

        // 5) Confirmar contraseña en tiempo real
        binding.etConfirmPasswordAdmin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirm = s.toString()
                val original = binding.etPasswordAdmin.text.toString()
                binding.tilConfirmPasswordAdmin.error = when {
                    confirm.isEmpty() ->
                        "Campo obligatorio"
                    confirm != original ->
                        "La contraseña no coincide"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
        })
    }

    private fun setupListeners() {
        binding.btnRegisterAdmin.setOnClickListener {
            // Si hay errores activos, no continuar
            val hasError = listOf(
                binding.tilFirstNameAdmin,
                binding.tilLastNamePatAdmin,
                binding.tilLastNameMatAdmin,
                binding.tilEmailAdmin,
                binding.tilPasswordAdmin,
                binding.tilConfirmPasswordAdmin
            ).any { it.error != null }

            if (hasError) return@setOnClickListener

            val fn    = binding.etFirstNameAdmin.text.toString().trim()
            val lp    = binding.etLastNamePatAdmin.text.toString().trim()
            val lm    = binding.etLastNameMatAdmin.text.toString().trim()
            val email = binding.etEmailAdmin.text.toString().trim()
            val pw    = binding.etPasswordAdmin.text.toString().trim()

            viewModel.registerAdmin(fn, lp, lm, email, pw)
        }

        binding.btnBackToLoginAdmin.setOnClickListener {
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
                // error genérico lo mostramos bajo contraseña
                binding.tilPasswordAdmin.error = result.errorMessage
            }
        }
    }
}
