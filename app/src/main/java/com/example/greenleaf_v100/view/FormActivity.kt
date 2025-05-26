package com.example.greenleaf_v100.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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

        setupSpinners()

        binding.btnSeleccionarImagen.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnAgregar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val descripcion = binding.etDescripcion.text.toString()
            val precio = binding.etPrecio.text.toString()
            val consejo = binding.etConsejo.text.toString()
            val spnTipo = binding.spnTipo.selectedItem.toString()
            val spnEstancia = binding.spnEstancia.selectedItem.toString()
            val spnRiego = binding.spnRiego.selectedItem.toString()
            val stockStr = binding.etStock.text.toString().trim()
            val stock = stockStr.toIntOrNull()

            if (stock == null) {
                Toast.makeText(this, "Stock debe ser un número válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (imageUri == null || nombre.isBlank() || descripcion.isBlank() || precio.isBlank() || consejo.isBlank() )  {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (spnTipo == "Tipo:" || spnEstancia == "Estancia:" || spnRiego == "Riego:") {
                Toast.makeText(this, "Seleccione todas las opciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            subirImagenAFirebase(imageUri!!) { urlImagen ->
                guardarPlantaEnFirestore(nombre, descripcion, precio, urlImagen, spnTipo, spnEstancia, spnRiego, consejo, stock)
            }
        }

        // Paso 1 → Paso 2
        binding.btnSiguiente.setOnClickListener {
            if (validarPaso1()) {
                binding.formStep1.visibility = View.GONE
                binding.btnSiguiente.visibility = View.GONE
                binding.formStep2.visibility = View.VISIBLE
                binding.btnAgregar.visibility = View.VISIBLE
            }
        }

        // Paso 2 → Paso 1
        binding.btnVolver.setOnClickListener {
            binding.formStep2.visibility = View.GONE
            binding.btnAgregar.visibility = View.GONE
            binding.formStep1.visibility = View.VISIBLE
            binding.btnSiguiente.visibility = View.VISIBLE
        }


    }

    private fun setupSpinners() {
        val spnTipo = listOf("Tipo:", "Sol", "Sombra", "Sombra Parcial")
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spnTipo)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnTipo.adapter = adapter1

        val spnEstancia = listOf("Estancia:", "Interior", "Exterior")
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spnEstancia)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnEstancia.adapter = adapter2

        val spnRiego = listOf("Riego:", "Alto", "Frecuente", "Bajo")
        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spnRiego)
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnRiego.adapter = adapter3
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

    private fun guardarPlantaEnFirestore(
        nombre: String,
        descripcion: String,
        precio: String,
        urlImagen: String,
        spnTipo: String,
        spnEstancia: String,
        spnRiego: String,
        consejo: String,
        stock: Int)
    {
        val nuevoDocumento = Firebase.firestore.collection("plantas").document()

        val planta = ModelPlanta(
            id = nuevoDocumento.id,
            nombre,
            descripcion,
            precio,
            urlImagen,
            spnTipo,       // categoria
            spnEstancia,   // tipoCuidado
            spnRiego,       // nivelDificultad
            consejo,
            stock,

        )

        // 3. Guardamos usando .set() en la referencia creada
        nuevoDocumento.set(planta)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Planta guardada con ID: ${nuevoDocumento.id}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CatalogoActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun validarPaso1(): Boolean {
        val nombre = binding.etNombre.text.toString().trim()
        val precio = binding.etPrecio.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val consejo = binding.etConsejo.text.toString().trim()
        val stock = binding.etStock.text.toString().trim()

        if (nombre.isEmpty() || precio.isEmpty() || descripcion.isEmpty() || consejo.isEmpty() || stock.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos del paso 1", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}