package com.example.greenleaf_v100.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class RegistroResult_C(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class RegistroClientesViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _registroResult = MutableLiveData<RegistroResult_C>()
    val registroResult: LiveData<RegistroResult_C> = _registroResult

    fun registerCliente(
        nombre: String,
        paterno: String,
        materno: String,
        email: String,
        password: String
    ) {
        if (nombre.isBlank() || paterno.isBlank() || materno.isBlank() ||
            email.isBlank()  || password.isBlank()
        ) {
            _registroResult.value = RegistroResult_C(false, "Todos los campos son obligatorios")
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
                    "tipoUsuario" to "clientes",
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // Guardar en la colección "clientes"
                db.collection("clientes")
                    .document(uid)
                    .set(data)
                    .addOnSuccessListener {
                        _registroResult.value = RegistroResult_C(true)
                    }
                    .addOnFailureListener { e ->
                        _registroResult.value = RegistroResult_C(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _registroResult.value = RegistroResult_C(false, e.localizedMessage)
            }
    }
}