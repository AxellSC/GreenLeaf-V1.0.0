package com.example.greenleaf_v100.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenleaf_v100.GreenLeafDbHelper
import com.example.greenleaf_v100.databinding.ActivityFavoritosBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.example.greenleaf_v100.model.PlantasAdapter

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var adapter: PlantasAdapter
    private lateinit var favoritos: List<ModelPlanta>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        adapter = PlantasAdapter(emptyList()) { planta ->
            // Acción al hacer clic en un favorito (opcional)
            Toast.makeText(this, "Seleccionaste: ${planta.nombre}", Toast.LENGTH_SHORT).show()
        } // Puedes adaptar esto a tu lógica si tiene clickListener
        binding.recyclerFavoritos.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavoritos.adapter = adapter

        // Cargar favoritos desde SQLite
        val dbHelper = GreenLeafDbHelper(this)
        val dao = dbHelper.getFavoritoDao()
        favoritos = dao.obtenerFavoritos()
        adapter.actualizarLista(favoritos)
    }
}
