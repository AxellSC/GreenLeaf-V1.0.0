package com.example.greenleaf_v100.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
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
    private val firestore = FirebaseFirestore.getInstance()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid == null) {
                    _loginResult.value = LoginResult(false, errorMessage = "No se obtuvo UID de usuario.")
                    return@addOnSuccessListener
                }
                // Si el usuario se autenticó, vamos a Firestore a leer "role"
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val roleString = documentSnapshot.getString("role") ?: "CLIENTE"
                        val userType = if (roleString.uppercase() == "ADMIN")
                            UserType.ADMIN else UserType.CLIENTE
                        _loginResult.value = LoginResult(true, userType)
                    }
                    .addOnFailureListener { e ->
                        // Se autenticó, pero no pudimos leer role
                        _loginResult.value = LoginResult(
                            false,
                            errorMessage = "Error al obtener rol: ${e.message}"
                        )
                    }
            }
            .addOnFailureListener { e ->
                // Mapear errores de FirebaseAuth a mensajes en español
                val msg = when (e) {
                    is FirebaseAuthInvalidUserException ->
                        when (e.errorCode) {
                            "ERROR_USER_NOT_FOUND"   -> "No existe una cuenta con este correo."
                            "ERROR_USER_DISABLED"    -> "La cuenta ha sido desactivada."
                            else                     -> "Usuario inválido."
                        }
                    is FirebaseAuthInvalidCredentialsException ->
                        when (e.errorCode) {
                            "ERROR_WRONG_PASSWORD"   -> "Contraseña incorrecta."
                            "ERROR_INVALID_EMAIL"    -> "Formato de correo inválido."
                            else                     -> "El correo o contraseña es incorrecto."
                        }
                    is FirebaseNetworkException ->
                        "Error de red. Verifica tu conexión."
                    else ->
                        "Error de autenticación. Intenta de nuevo."
                }
                _loginResult.value = LoginResult(false, errorMessage = msg)
            }
    }
}
