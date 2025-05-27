package com.example.greenleaf_v100.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.GreenLeafDbHelper
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.data.dao.FavoritoDao
import com.example.greenleaf_v100.databinding.ActivityPresentacionPlantaBinding
import com.example.greenleaf_v100.model.ModelPlanta

class PresentacionPlantaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresentacionPlantaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresentacionPlantaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos del intent
        val id = intent.getStringExtra("ID") ?: ""
        val nombre = intent.getStringExtra("NOMBRE") ?: ""
        val descripcion = intent.getStringExtra("DESCRIPCION") ?: ""
        val consejo = intent.getStringExtra("CONSEJO") ?: ""
        val tipo = intent.getStringExtra("TIPO") ?: ""
        val precio = intent.getDoubleExtra("PRECIO", 0.0)
        val stock = intent.getIntExtra("STOCK", 0)
        val riego = intent.getStringExtra("RIEGO") ?: ""
        val estancia = intent.getStringExtra("ESTANCIA") ?: ""
        val fotoUrl = intent.getStringExtra("FOTO_URL") ?: ""

        // Mostrar info en pantalla
        binding.tvNombreP.text = nombre
        binding.tvDescripcionP.text = descripcion
        binding.tvConsejo.text = consejo
        binding.tvA.text = "Necesita $tipo"
        binding.tvB.text = "Ideal para $estancia"
        binding.tvC.text = "Ocupa un riego $riego"
        binding.tvStockP.text = "Cantidad disponible: $stock"

        Glide.with(this)
            .load(fotoUrl)
            .into(binding.ivPlantaP)

        binding.iconA.setImageResource(getIconoTipo(tipo))
        binding.iconB.setImageResource(getIconoEstancia(estancia))
        binding.iconC.setImageResource(getIconoRiego(riego))

        // Crear objeto planta
        val planta = ModelPlanta(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            consejo = consejo,
            tipo = tipo,
            precio = precio.toString(),
            stock = stock,
            riego = riego,
            estancia = estancia,
            fotoUrl = fotoUrl
        )

        // Configurar favoritos con SQLite
        val dbHelper = GreenLeafDbHelper(this)
        val dao = dbHelper.getFavoritoDao()

        // Cargar estado inicial
        binding.checkFavorito.isChecked = dao.esFavorito(id)

        // Escuchar cambios
        binding.checkFavorito.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dao.agregarFavorito(planta)
                Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
            } else {
                dao.eliminarFavorito(id)
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIconoTipo(tipo: String): Int = when (tipo.lowercase()) {
        "sol" -> R.drawable.icona_sol
        "sombra" -> R.drawable.icona_sombra
        "sombra parcial" -> R.drawable.icona_solysombra
        else -> R.drawable.icona_solysombra
    }

    private fun getIconoEstancia(estancia: String): Int = when (estancia.lowercase()) {
        "interior" -> R.drawable.iconb_interior
        "exterior" -> R.drawable.iconb_exterior
        else -> R.drawable.iconb_exterior
    }

    private fun getIconoRiego(riego: String): Int = when (riego.lowercase()) {
        "alto" -> R.drawable.iconc_altor
        "moderado", "bajo" -> R.drawable.iconc_bajor
        else -> R.drawable.iconc_bajor
    }
}
