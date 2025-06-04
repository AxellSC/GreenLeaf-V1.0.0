package com.example.greenleaf_v100.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // LiveData para nombre (para “Bienvenido NombreUsuario!”)
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    // LiveData para correo
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    // LiveData para rol (“Cliente” / “Administrador”)
    private val _role = MutableLiveData<String>()
    val role: LiveData<String> = _role

    // LiveData para foto de perfil (URI)
    private val _photoUri = MutableLiveData<Uri?>()
    val photoUri: LiveData<Uri?> = _photoUri

    // LiveData para contraseña encriptada (simplemente “********”)
    private val _passwordMasked = MutableLiveData<String>()
    val passwordMasked: LiveData<String> = _passwordMasked

    // LiveData para mensajes de error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }
    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: run {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        // Correo
        _email.value = user.email ?: ""

        // Nombre (si existe en displayName, sino en Firestore “users/{uid}.name”)
        if (!user.displayName.isNullOrBlank()) {
            _userName.value = user.displayName!!
        } else {
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    _userName.value = doc.getString("name") ?: ""
                }
                .addOnFailureListener {
                    _userName.value = ""
                }
        }

        // Rol (Firestore: users/{uid}.role)
        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                _role.value = doc.getString("role") ?: "Cliente"
            }
            .addOnFailureListener {
                _role.value = "Cliente"
            }

        // Foto de perfil (si en FirebaseAuth hay photoUrl, la usamos)
        if (user.photoUrl != null) {
            _photoUri.value = user.photoUrl
        } else {
            _photoUri.value = null
        }

        // Contraseña (no la obtenemos textualmente, solo mostramos “********”)
        _passwordMasked.value = "********"
    }

    /**
     * Actualiza el correo en FirebaseAuth y en Firestore (campo “email”).
     */
    fun actualizarCorreo(nuevoEmail: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }

        user.updateEmail(nuevoEmail)
            .addOnSuccessListener {
                // Actualizar en Firestore (opcional, si guardas email allí)
                firestore.collection("users")
                    .document(user.uid)
                    .update("email", nuevoEmail)
                _email.value = nuevoEmail
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    /**
     * Re-autentica con la contraseña actual y luego actualiza a la nueva contraseña.
     */
    fun actualizarContrasena(actual: String, nuevo: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        val cred = EmailAuthProvider.getCredential(user.email ?: "", actual)
        user.reauthenticate(cred)
            .addOnSuccessListener {
                user.updatePassword(nuevo)
                    .addOnSuccessListener {
                        _passwordMasked.value = "********"
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Re-autenticación fallida: ${e.localizedMessage}")
            }
    }

    /**
     * Sube la imagen seleccionada a Storage y actualiza photoUrl en FirebaseAuth.
     */
    fun actualizarFotoPerfil(uriLocal: Uri, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        val uid = user.uid
        val storageRef = storage.reference.child("users/$uid/profile.jpg")

        storageRef.putFile(uriLocal)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { descargaUri ->
                        val request = UserProfileChangeRequest.Builder()
                            .setPhotoUri(descargaUri)
                            .build()
                        user.updateProfile(request)
                            .addOnSuccessListener {
                                _photoUri.value = descargaUri
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                onComplete(false, e.localizedMessage)
                            }
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    /** Cierra sesión en FirebaseAuth. */
    fun logout() {
        auth.signOut()
    }
}
