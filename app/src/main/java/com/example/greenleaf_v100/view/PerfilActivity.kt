package com.example.greenleaf_v100.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityPerfilBinding
import com.example.greenleaf_v100.viewmodel.PerfilViewModel

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var viewModel: PerfilViewModel

    // 1) Lanzador para el selector de imágenes (galería)
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uriSeleccionada: Uri? = result.data!!.data
            uriSeleccionada?.let { uri ->
                // Llamamos al ViewModel para que suba la imagen
                viewModel.subirImagenPerfil(uri) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(this, "Imagen de perfil actualizada.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al subir imagen: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2) Instanciamos DataBinding
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3) Obtenemos el ViewModel
        viewModel = ViewModelProvider(this)[PerfilViewModel::class.java]

        // 4) Asignamos el ViewModel al binding y el lifecycle owner
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // 5) Observamos cambios en la URL de la imagen para cargar con Glide
        viewModel.profileImageUrl.observe(this) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .into(binding.ivProfile)
            } else {
                // Si es nulo, dejamos la imagen por defecto (ic_default_profile)
                binding.ivProfile.setImageResource(R.drawable.imagen_user)
            }
        }

        // 6) Click listener para “+” (abrir galería)
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // 7) Toggle mostrar/ocultar contraseña
        binding.btnTogglePassword.setOnClickListener {
            viewModel.togglePasswordVisibility()
        }

        // 8) “Cambiar correo”
        binding.tvChangeEmail.setOnClickListener {
            mostrarDialogoCambiarCorreo()
        }

        // 9) “Cambiar contraseña”
        binding.tvChangePassword.setOnClickListener {
            mostrarDialogoCambiarPassword()
        }

        // 10) Cerrar sesión
        binding.btnLogout.setOnClickListener {
            viewModel.cerrarSesion()
            // Volver al Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    /** Muestra un AlertDialog simple para solicitar el nuevo correo **/
    private fun mostrarDialogoCambiarCorreo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar correo")
        // Layout simple: un EditText para nuevo email
        val input = android.widget.EditText(this)
        input.hint = "Ingresa correo nuevo"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nuevoEmail = input.text.toString().trim()
            if (nuevoEmail.isNotEmpty()) {
                viewModel.cambiarEmail(nuevoEmail) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(this, "Correo actualizado.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "El campo no puede estar vacío.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    /** Muestra un AlertDialog para cambiar contraseña: pide actual y nueva **/
    private fun mostrarDialogoCambiarPassword() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar contraseña")

        // Creamos un LinearLayout vertical con dos EditText
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 0)
        }

        val etActual = android.widget.EditText(this).apply {
            hint = "Contraseña actual"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val etNueva = android.widget.EditText(this).apply {
            hint = "Contraseña nueva"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(etActual)
        layout.addView(etNueva)

        builder.setView(layout)
        builder.setPositiveButton("Guardar") { dialog, _ ->
            val actual = etActual.text.toString().trim()
            val nueva = etNueva.text.toString().trim()
            if (actual.isNotEmpty() && nueva.isNotEmpty()) {
                viewModel.cambiarPassword(actual, nueva) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(this, "Contraseña actualizada.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Ambos campos son obligatorios.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        // En caso de que el usuario ya haya cerrado sesión, podríamos redirigirlo al Login.
        if (viewModel.email.value == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
