package com.example.greenleaf_v100.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenleaf_v100.model.ModelAdmin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Representa el resultado del login:
 * - isSuccess = true si autenticación OK
 * - errorMessage con texto si falla
 */
data class LoginResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _loginResult   = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // LiveData para exponer el perfil del admin
    private val _adminProfile = MutableLiveData<ModelAdmin>()
    val adminProfile: LiveData<ModelAdmin> = _adminProfile

    /** Intenta autenticar con FirebaseAuth y luego carga perfil desde Firestore */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult(false, "Correo y contraseña obligatorios")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authRes ->
                val uid = authRes.user?.uid ?: run {
                    _loginResult.value = LoginResult(false, "UID no disponible")
                    return@addOnSuccessListener
                }
                // Leer datos del admin tras login exitoso
                db.collection("admins")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        // toObject devuelve ModelAdmin?
                        val admin = doc.toObject(ModelAdmin::class.java)

                        // 1) Opción segura con let:
                        admin?.let {
                            _adminProfile.value = it
                            _loginResult .value = LoginResult(true)
                        } ?: run {
                            _loginResult.value = LoginResult(false, "Perfil no encontrado")
                        }
                    }
                    .addOnFailureListener { e ->
                        _loginResult.value = LoginResult(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { exc ->
                _loginResult.value = LoginResult(false, exc.localizedMessage)
            }
    }
}