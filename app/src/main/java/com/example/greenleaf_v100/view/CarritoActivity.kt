package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityCarritoBinding
import com.example.greenleaf_v100.databinding.ActivityCatalogoBinding
import com.example.greenleaf_v100.databinding.ActivityPerfilBinding
import com.example.greenleaf_v100.databinding.ActivityVentasBinding
import com.example.greenleaf_v100.viewmodel.UserType

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }

        if (tipoUsuario == UserType.CLIENTE) {
            // Mostrar opciones admin
            binding.barraCliente.selectedItemId = R.id.nav_carrito
            binding.barraCliente.visibility = View.VISIBLE

        } else if (tipoUsuario == UserType.ADMIN) {

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
    }

}