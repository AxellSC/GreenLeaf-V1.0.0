package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityCatalogoBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.example.greenleaf_v100.model.PlantasAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.greenleaf_v100.viewmodel.UserType

class CatalogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var adapter: PlantasAdapter
    private val plantasList = mutableListOf<ModelPlanta>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }

        if (tipoUsuario == UserType.ADMIN) {
            // Mostrar opciones de admin
        } else if (tipoUsuario == UserType.CLIENTE) {
            // Mostrar opciones para cliente
            binding.btnAgregarPlanta.visibility = View.INVISIBLE

        } else {
            // Tipo desconocido o no recibido
        }

        setupRecyclerView()
        cargarPlantas()

        binding.btnAgregarPlanta.setOnClickListener{
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    // Ya estás en esta Activity
                    true
                }
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Marcar el ítem actual seleccionado (por ejemplo, si estás en InicioActivity)
        binding.bottomNavigationView.selectedItemId = R.id.nav_inicio

    }

    private fun setupRecyclerView() {
        adapter = PlantasAdapter(plantasList) { planta ->
            val intent = Intent(this, PresentacionPlantaActivity::class.java).apply {
                putExtra("PLANTA_ID", planta.id)
                putExtra("NOMBRE", planta.nombre)
                putExtra("DESCRIPCION",planta.descripcion)
                putExtra("FOTO_URL", planta.fotoUrl)
                putExtra("TIPO", planta.tipo)
                putExtra("ESTANCIA", planta.estancia)
                putExtra("RIEGO", planta.riego)
                putExtra("CONSEJO",planta.consejo)

            }
            startActivity(intent)
        }

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
}