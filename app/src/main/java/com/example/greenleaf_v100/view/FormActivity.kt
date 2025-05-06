package com.example.greenleaf_v100.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityFormBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

class FormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBinding
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        binding.ivPreview.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSeleccionarImagen.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnAgregar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val descripcion = binding.etDescripcion.text.toString()
            val precio = binding.etPrecio.text.toString()

            if (imageUri == null || nombre.isBlank() || descripcion.isBlank() || precio.isBlank()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            subirImagenAFirebase(imageUri!!) { urlImagen ->
                guardarPlantaEnFirestore(nombre, descripcion, precio, urlImagen)
            }
        }


    }

    private fun subirImagenAFirebase(uri: Uri, callback: (String) -> Unit) {
        val storageRef = Firebase.storage.reference
        val nombreArchivo = "plantas/${UUID.randomUUID()}.jpg"
        val imagenRef = storageRef.child(nombreArchivo)

        imagenRef.putFile(uri)
            .addOnSuccessListener {
                imagenRef.downloadUrl.addOnSuccessListener { uriDescarga ->
                    callback(uriDescarga.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarPlantaEnFirestore(nombre: String, descripcion: String, precio: String, urlImagen: String) {
        val db = Firebase.firestore
        val planta = ModelPlanta(
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            fotoUrl = urlImagen
        )

        db.collection("plantas")
            .add(planta)
            .addOnSuccessListener {
                Toast.makeText(this, "Planta guardada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CatalogoActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar planta", Toast.LENGTH_SHORT).show()
            }
    }
}