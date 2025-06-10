package com.example.greenleaf_v100.view

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityPerfilBinding
import com.example.greenleaf_v100.viewmodel.PerfilViewModel
import com.example.greenleaf_v100.viewmodel.UserType
import com.google.firebase.auth.FirebaseAuth


class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var viewModel: PerfilViewModel




    // Launcher para elegir foto desde galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val uri = resultado.data?.data
            if (uri != null) {
                viewModel.actualizarFotoPerfil(uri) { exito, error ->
                    if (exito) {
                        Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.photo_pick_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[PerfilViewModel::class.java]

        observarLiveData()
        configurarListeners()

        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }

        if (tipoUsuario == UserType.ADMIN) {
            // Mostrar opciones admin
            binding.barraAdmin.selectedItemId = R.id.nav_perfil
            binding.barraAdmin.visibility = View.VISIBLE
            binding.barraCliente.visibility = View.INVISIBLE

        } else if (tipoUsuario == UserType.CLIENTE){
            binding.barraCliente.selectedItemId = R.id.nav_perfil
            binding.barraCliente.visibility = View.VISIBLE
            binding.barraAdmin.visibility = View.INVISIBLE
        }

        //Barra de navegacion Cliente

        binding.barraCliente.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_carrito -> {
                    val intent = Intent(this, CarritoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        //Bara de Admin

        binding.barraAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_ventas -> {
                    val intent = Intent(this, VentasActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    private fun observarLiveData() {
        // 1) Nombre → tvGreeting
        viewModel.userName.observe(this) { nombre ->
            binding.tvGreeting.text = getString(R.string.hola_usuario, nombre)
        }

        // 2) Correo → tvEmail
        viewModel.email.observe(this) { correo ->
            binding.tvEmail.text = correo
        }

        // 3) Rol → tvRole
        viewModel.role.observe(this) { rol ->
            binding.tvRole.text = rol
        }

        // 4) Foto → ivProfile
        viewModel.photoUri.observe(this) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfile)
            } else {
                binding.ivProfile.setImageResource(R.drawable.usuario_foto)
            }
        }

        // 5) Contraseña “enmascarada” → tvPassword
        viewModel.passwordMasked.observe(this) { masked ->
            binding.tvPassword.text = masked
        }

        // 6) Errores → mostrar Toast
        viewModel.errorMessage.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                // Ahora, en lugar de asignar errorMessage.value = null,
                // llamamos al método público del ViewModel que lo limpia:
                viewModel.clearError()
            }
        }
    }

    private fun configurarListeners() {
        // ------------------------
        // 1) Cambiar foto (FAB)
        // ------------------------
        binding.fabEditPhoto.setOnClickListener {
            // Abrir galería
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // ------------------------
        // 2) Cambiar correo
        // ------------------------
        binding.btnEditEmail.setOnClickListener {
            mostrarDialogoCambioCorreo()
        }

        // ------------------------
        // 3) Cambiar contraseña
        // ------------------------
        binding.btnEditPwd.setOnClickListener {
            mostrarDialogoCambioPassword()
        }

        // ------------------------
        // 4) Cerrar sesión
        // ------------------------
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Volver a LoginActivity (asegúrate de que exista)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    /**
     * Muestra un AlertDialog que pide al usuario el NUEVO correo.
     */
    private fun mostrarDialogoCambioCorreo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.change_email_title))

        val input = EditText(this)
        input.hint = getString(R.string.enter_new_email)
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.update)) { dialog, _ ->
            val nuevoEmail = input.text.toString().trim()
            if (nuevoEmail.isEmpty() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoEmail).matches()
            ) {
                binding.tvEmail.error = getString(R.string.error_invalid_email)
            } else {
                viewModel.actualizarCorreo(nuevoEmail) { exito, error ->
                    if (exito) {
                        Toast.makeText(this, "Correo actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    /**
     * Muestra un AlertDialog con dos EditText:
     *  - Contraseña actual (etCurrentPassword)
     *  - Nueva contraseña (etNewPassword)
     */
    private fun mostrarDialogoCambioPassword() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etActual = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNuevo = dialogView.findViewById<EditText>(R.id.etNewPassword)

        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_password_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.update)) { dialog, _ ->
                val actual = etActual.text.toString()
                val nuevo = etNuevo.text.toString()
                if (actual.isEmpty() || nuevo.length < 6) {
                    if (actual.isEmpty()) {
                        etActual.error = getString(R.string.error_invalid_password)
                    }
                    if (nuevo.length < 6) {
                        etNuevo.error = getString(R.string.error_invalid_password)
                    }
                } else {
                    viewModel.actualizarContrasena(actual, nuevo) { exito, error ->
                        if (exito) {
                            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
        builder.show()
    }
}
