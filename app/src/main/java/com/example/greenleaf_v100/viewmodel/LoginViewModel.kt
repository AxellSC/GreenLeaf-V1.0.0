package com.example.greenleaf_v100.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Identifica el tipo de usuario una vez logueado
enum class UserType { ADMIN, CLIENTE }

// Resultado de login con tipo
data class LoginResult(
    val isSuccess: Boolean,
    val userType: UserType? = null,
    val profileData: Map<String, Any>? = null,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult(false, errorMessage = "Correo y contraseña son obligatorios")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: run {
                    _loginResult.value = LoginResult(false, errorMessage = "UID inválido")
                    return@addOnSuccessListener
                }

                // Primero revisamos si es admin
                db.collection("admins").document(uid).get()
                    .addOnSuccessListener { docAdmin ->
                        if (docAdmin.exists()) {
                            // Usuario admin
                            _loginResult.value = LoginResult(
                                isSuccess   = true,
                                userType    = UserType.ADMIN,
                                profileData = docAdmin.data
                            )
                        } else {
                            // Si no es admin, revisamos clientes
                            db.collection("clientes").document(uid).get()
                                .addOnSuccessListener { docCliente ->
                                    if (docCliente.exists()) {
                                        _loginResult.value = LoginResult(
                                            isSuccess   = true,
                                            userType    = UserType.CLIENTE,
                                            profileData = docCliente.data
                                        )
                                    } else {
                                        // No está en ninguna colección
                                        _loginResult.value = LoginResult(
                                            false,
                                            errorMessage = "Usuario no encontrado en admins ni clientes"
                                        )
                                    }
                                }
                                .addOnFailureListener { e ->
                                    _loginResult.value = LoginResult(false, errorMessage = e.localizedMessage)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        _loginResult.value = LoginResult(false, errorMessage = e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _loginResult.value = LoginResult(false, errorMessage = e.localizedMessage)
            }
    }
}
