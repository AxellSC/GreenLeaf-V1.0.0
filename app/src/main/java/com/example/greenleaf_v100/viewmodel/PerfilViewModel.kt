package com.example.greenleaf_v100.viewmodel


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PerfilViewModel : ViewModel() {

    // Firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // LiveData para datos del usuario
    private val _userName = MutableLiveData<String>("Usuario")
    val userName: LiveData<String> = _userName

    private val _email = MutableLiveData<String>("correo@dominio.com")
    val email: LiveData<String> = _email

    private val _role = MutableLiveData<String>("cliente")
    val role: LiveData<String> = _role

    // Para la imagen de perfil (URL en Firebase Storage o placeholder)
    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    // Contraseña (no podemos obtener la real, así que usamos asteriscos)
    private val _realPassword = MutableLiveData<String>("********")
    val realPassword: LiveData<String> = _realPassword

    // Para alternar visibilidad de la contraseña
    private val _isPasswordVisible = MutableLiveData<Boolean>(false)
    val isPasswordVisible: LiveData<Boolean> = _isPasswordVisible

    init {
        cargarDatosUsuario()
    }

    /** 1) Carga inicial de datos desde Firestore y Auth **/
    private fun cargarDatosUsuario() {
        val user: FirebaseUser? = auth.currentUser
        if (user == null) {
            // No hay sesión iniciada; podría manejarlo en la Activity
            return
        }

        // 1.a) Nombre, rol e imagen (la colección se asume "users" con doc = uid)
        val uid = user.uid
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _userName.value = doc.getString("name") ?: "Usuario"
                    _role.value = doc.getString("role") ?: "cliente"
                    _profileImageUrl.value = doc.getString("profileImageUrl")
                    // Email viene de FirebaseAuth, pero lo ponemos también
                    _email.value = user.email
                }
            }
            .addOnFailureListener {
                // Error; dejamos valores por defecto o los guardamos por separado
                _email.value = user.email
            }
    }

    /** 2) Alternar visibilidad de contraseña (solo cambia máscara) **/
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = _isPasswordVisible.value?.not()
    }

    /** 3) Cambiar email en FirebaseAuth y Firestore **/
    fun cambiarEmail(nuevoEmail: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "No hay usuario autenticado.")
            return
        }
        // Actualizar en FirebaseAuth
        user.updateEmail(nuevoEmail)
            .addOnSuccessListener {
                // Actualizar el campo “email” en Firestore (en doc de usuario)
                firestore.collection("users")
                    .document(user.uid)
                    .update("email", nuevoEmail)
                    .addOnSuccessListener {
                        _email.value = nuevoEmail
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * 4) Cambiar contraseña en FirebaseAuth.
     *    Firebase requiere re-autenticación para cambiar password.
     *    Aquí se asume que en la Activity se piden la contraseña actual y la nueva contraseña.
     */
    fun cambiarPassword(
        contrasenaActual: String,
        contrasenaNueva: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "No hay usuario autenticado.")
            return
        }

        val emailActual = user.email
        if (emailActual.isNullOrBlank()) {
            onResult(false, "Email no disponible.")
            return
        }

        // 4.a) Re-autenticación
        val credential = EmailAuthProvider.getCredential(emailActual, contrasenaActual)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // 4.b) Actualizar contraseña
                user.updatePassword(contrasenaNueva)
                    .addOnSuccessListener {
                        // En este punto, reemplazamos la máscara en caso de querer visualizarla
                        _realPassword.value = "********"
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, "Re-autenticación falló: ${e.message}")
            }
    }

    /** 5) Subir imagen seleccionada a Firebase Storage y actualizar Firestore **/
    fun subirImagenPerfil(uriImagen: Uri, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onResult(false, "Usuario no autenticado.")
            return
        }

        val referenciaStorage = storage.reference
            .child("profile_images/${user.uid}.jpg")

        referenciaStorage.putFile(uriImagen)
            .addOnSuccessListener {
                referenciaStorage.downloadUrl
                    .addOnSuccessListener { descargaUri ->
                        // Guardar URL en Firestore
                        firestore.collection("users")
                            .document(user.uid)
                            .update("profileImageUrl", descargaUri.toString())
                            .addOnSuccessListener {
                                _profileImageUrl.value = descargaUri.toString()
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /** 6) Cerrar sesión **/
    fun cerrarSesion() {
        auth.signOut()
    }

}
