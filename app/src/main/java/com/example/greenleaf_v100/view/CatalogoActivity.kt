package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityCatalogoBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.example.greenleaf_v100.model.PlantasAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.greenleaf_v100.viewmodel.UserType
import com.google.firebase.storage.FirebaseStorage
import kotlin.random.Random

class CatalogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var adapter: PlantasAdapter
    private val plantasList = mutableListOf<ModelPlanta>()

    private var plantaDestacada: ModelPlanta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }

        if (tipoUsuario == UserType.ADMIN) {
            // Mostrar opciones de admin
            binding.barraAdmin.selectedItemId = R.id.nav_inicio
            binding.barraAdmin.visibility = View.VISIBLE
            binding.barraCliente.visibility = View.INVISIBLE
        } else if (tipoUsuario == UserType.CLIENTE) {
            // Mostrar opciones para cliente
            binding.barraCliente.selectedItemId = R.id.nav_inicio
            binding.btnAgregarPlanta.visibility = View.INVISIBLE
            binding.barraCliente.visibility = View.VISIBLE
            binding.barraAdmin.visibility = View.INVISIBLE

        } else {
            // Tipo desconocido o no recibido
        }

        setupRecyclerView()
        cargarPlantas()

        binding.btnAgregarPlanta.setOnClickListener{
            val intent = Intent(this, FormActivity::class.java)

            startActivity(intent)
        }

        binding.barraCliente.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_carrito -> {
                    val intent = Intent(this, CarritoClienteActivity::class.java)
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

        binding.barraAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    // Ya estás en esta Activity
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


       // binding.bottomNavigationView.selectedItemId = R.id.nav_inicio

        binding.cardViewRandom.setOnClickListener {
            plantaDestacada?.let { planta ->
                val intent = Intent(this, PresentacionPlantaActivity::class.java).apply {
                    putExtra("PLANTA_ID", planta.id)
                    putExtra("NOMBRE", planta.nombre)
                    putExtra("DESCRIPCION", planta.descripcion)
                    putExtra("FOTO_URL", planta.fotoUrl)
                    putExtra("TIPO", planta.tipo)
                    putExtra("ESTANCIA", planta.estancia)
                    putExtra("RIEGO", planta.riego)
                    putExtra("CONSEJO", planta.consejo)
                    putExtra("STOCK", planta.stock)
                }
                startActivity(intent)
            }
        }

    }

    private fun setupRecyclerView() {
        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }
        val esAdmin = tipoUsuario == UserType.ADMIN

        adapter = PlantasAdapter(
            plantasList,
            onItemClick = { planta ->
                val intent = Intent(this, PresentacionPlantaActivity::class.java).apply {
                    putExtra("PLANTA_ID", planta.id)
                    putExtra("NOMBRE", planta.nombre)
                    putExtra("DESCRIPCION", planta.descripcion)
                    putExtra("FOTO_URL", planta.fotoUrl)
                    putExtra("TIPO", planta.tipo)
                    putExtra("ESTANCIA", planta.estancia)
                    putExtra("RIEGO", planta.riego)
                    putExtra("CONSEJO", planta.consejo)
                    putExtra("STOCK", planta.stock)
                }
                startActivity(intent)
            },
            onDeleteClick = { planta ->
                eliminarPlanta(planta)
            },
            isAdmin = esAdmin
        )


        // GridLayoutManager con 2 columnas
        val gridLayoutManager = GridLayoutManager(this, 2) // <- Número de columnas
        with(binding.recyclerView) {
            layoutManager = gridLayoutManager // Asigna el GridLayoutManager
            adapter = this@CatalogoActivity.adapter

        }
    }

    private fun cargarPlantas() {
        FirebaseFirestore.getInstance().collection("plantas")
            .get()
            .addOnSuccessListener { result ->
                plantasList.clear()
                for (document in result) {
                    val planta = document.toObject(ModelPlanta::class.java).apply {
                        id = document.id
                    }
                    plantasList.add(planta)
                }
                adapter.notifyDataSetChanged()

                if (plantasList.isNotEmpty()) {
                    val indiceAleatorio = (0 until plantasList.size).random()
                    val planta = plantasList[indiceAleatorio]
                    plantaDestacada = planta
                    binding.tvNombrePC.text = planta.nombre
                    binding.tvDescripcionC.text = planta.descripcion
                    Glide.with(this).load(planta.fotoUrl).into(binding.ivPlantaC)
                }

                //Mostrar mensaje si no hay datos
                binding.tvEmptyView.visibility = if (plantasList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { exception ->
                binding.tvEmptyView.visibility = View.VISIBLE
                Toast.makeText(
                    this,
                    "Error al cargar plantas: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun eliminarPlanta(planta: ModelPlanta) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar planta")
            .setMessage("¿Estás seguro de que deseas eliminar esta planta?")
            .setPositiveButton("Sí") { _, _ ->

                // 1. Obtener referencia de la imagen desde la URL
                planta.fotoUrl?.let { url ->
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)

                    // 2. Eliminar la imagen
                    storageRef.delete()
                        .addOnSuccessListener {
                            // 3. Si se elimina la imagen, eliminar el documento en Firestore
                            FirebaseFirestore.getInstance().collection("plantas")
                                .document(planta.id!!)
                                .delete()
                                .addOnSuccessListener {
                                    plantasList.remove(planta)
                                    adapter.notifyDataSetChanged()
                                    Toast.makeText(this, "Planta eliminada", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al eliminar planta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar imagen", Toast.LENGTH_SHORT)
                                .show()
                        }
                } ?: run {
                    // Si no tiene imagen (url null), solo eliminar de Firestore
                    FirebaseFirestore.getInstance().collection("plantas")
                        .document(planta.id!!)
                        .delete()
                        .addOnSuccessListener {
                            plantasList.remove(planta)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(
                                this,
                                "Planta eliminada (sin imagen)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar planta", Toast.LENGTH_SHORT)
                                .show()
                        }
                }

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}