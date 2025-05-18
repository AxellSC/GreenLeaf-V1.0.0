package com.example.greenleaf_v100.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class RegistroResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class RegistroAdminViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _registroResult = MutableLiveData<RegistroResult>()
    val registroResult: LiveData<RegistroResult> = _registroResult

    fun registerAdmin(
        nombre: String,
        paterno: String,
        materno: String,
        email: String,
        password: String
    ) {
        if (nombre.isBlank() || paterno.isBlank() || materno.isBlank() ||
            email.isBlank()  || password.isBlank()
        ) {
            _registroResult.value = RegistroResult(false, "Todos los campos son obligatorios")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Obtener UID del nuevo usuario
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                // Preparar datos a guardar
                val data = mapOf(
                    "nombre"   to nombre,
                    "paterno"  to paterno,
                    "materno"  to materno,
                    "email"    to email,
                    "tipoUsuario" to "administrador",
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // Guardar en la colección "admins"
                db.collection("admins")
                    .document(uid)
                    .set(data)
                    .addOnSuccessListener {
                        _registroResult.value = RegistroResult(true)
                    }
                    .addOnFailureListener { e ->
                        _registroResult.value = RegistroResult(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _registroResult.value = RegistroResult(false, e.localizedMessage)
            }
    }
}