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

class CatalogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var adapter: PlantasAdapter
    private val plantasList = mutableListOf<ModelPlanta>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        cargarPlantas()

        binding.btnAgregarPlanta.setOnClickListener{
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = PlantasAdapter(plantasList) { planta ->
            val intent = Intent(this, DetallePlantaActivity::class.java).apply {
                putExtra("PLANTA_ID", planta.id)
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