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
import com.example.greenleaf_v100.viewmodel.CarritoViewModel
import androidx.activity.viewModels
import com.example.greenleaf_v100.model.CartItem
import com.google.firebase.auth.FirebaseAuth

class PresentacionPlantaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresentacionPlantaBinding

    // Guarda aquí la planta que construyes desde el Intent
    private lateinit var planta: ModelPlanta

    // Tu VM sigue igual, con setUserId(...) y cartItems
    private val cartViewModel: CarritoViewModel by viewModels()

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


        val cartViewModel: CarritoViewModel by viewModels()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no logueado")
        cartViewModel.setUserId(uid)

        // ⑤ Observa cartItems, no getUserCart
        cartViewModel.cartItems.observe(this) { list ->
            // Por ejemplo, actualiza el texto del botón si ya está en la lista
            val enCarrito = list.any { it.id == planta.id }
            binding.btnAddCart.text = if (enCarrito)
                "Eliminar del carrito"
            else
                "Añadir al carrito"
        }


        binding.btnAddCart.setOnClickListener {
            val item = CartItem(
                id       = planta.id,
                userId   = uid,
                name     = planta.nombre,
                price    = planta.precio.toDouble(),
                imageUrl = planta.fotoUrl
            )
            if (binding.btnAddCart.text == "Añadir al carrito") {
                cartViewModel.addToCart(item)
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            } else {
                cartViewModel.removeFromCart(item)
                Toast.makeText(this, "Eliminado del carrito", Toast.LENGTH_SHORT).show()
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
