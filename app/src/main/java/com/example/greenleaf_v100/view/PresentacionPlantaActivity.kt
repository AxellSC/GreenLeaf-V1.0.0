package com.example.greenleaf_v100.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityPresentacionPlantaBinding

class PresentacionPlantaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresentacionPlantaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresentacionPlantaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = intent.getStringExtra("NOMBRE") ?: ""
        val descripcion = intent.getStringExtra("DESCRIPCION") ?: ""
        val precio = intent.getDoubleExtra("PRECIO", 0.0)
        val fotoUrl = intent.getStringExtra("FOTO_URL") ?: ""
        val tipo = intent.getStringExtra("TIPO") ?: ""
        val estancia = intent.getStringExtra("ESTANCIA") ?: ""
        val riego = intent.getStringExtra("RIEGO") ?: ""
        val consejo = intent.getStringExtra("CONSEJO") ?: ""

        binding.tvNombreP.text = nombre
        binding.tvDescripcionP.text = descripcion
        binding.tvConsejo.text = consejo
        binding.tvA.text = "Necesita " + tipo
        binding.tvB.text = "Ideal para " + estancia
        binding.tvC.text = "Ocupa un riego " + riego

        Glide.with(this)
            .load(fotoUrl)
            .into(binding.ivPlantaP)

        binding.iconA.setImageResource(getIconoTipo(tipo))
        binding.iconB.setImageResource(getIconoEstancia(estancia))
        binding.iconC.setImageResource(getIconoRiego(riego))

    }

    private fun getIconoTipo(tipo: String): Int = when(tipo.lowercase()) {
        "sol" -> R.drawable.icona_sol
        "sombra" -> R.drawable.icona_sombra
        "sombra parcial" -> R.drawable.icona_solysombra
        else -> R.drawable.icona_solysombra
    }

    private fun getIconoEstancia(estancia: String): Int = when(estancia.lowercase()) {
        "interior" -> R.drawable.iconb_interior
        "exterior" -> R.drawable.iconb_exterior
        else -> R.drawable.iconb_exterior
    }

    private fun getIconoRiego(riego: String): Int = when(riego.lowercase()) {
        "alto" -> R.drawable.iconc_altor
        "moderado" -> R.drawable.iconc_bajor
        "bajo" -> R.drawable.iconc_bajor
        else -> R.drawable.iconc_bajor
    }

}