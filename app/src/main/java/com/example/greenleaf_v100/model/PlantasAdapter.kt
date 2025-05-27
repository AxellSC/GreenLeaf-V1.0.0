package com.example.greenleaf_v100.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ItemPlantaBinding

class PlantasAdapter(
    private var plantas: List<ModelPlanta> = listOf(),
    private val onItemClick: (ModelPlanta) -> Unit
) : RecyclerView.Adapter<PlantasAdapter.PlantaViewHolder>() {

    inner class PlantaViewHolder(private val binding: ItemPlantaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(planta: ModelPlanta) {
            with(binding) {
                // Cargar imagen con Glide
                Glide.with(root.context)
                    .load(planta.fotoUrl)
                    .into(ivPlanta)

                tvNombre.text = planta.nombre
                tvPrecio.text = "$${planta.precio}"
                tvDescripcion.text = planta.descripcion



                // Asignar iconos según características
                ivTipo.setImageResource(getIconoTipo(planta.tipo))
                ivEstancia.setImageResource(getIconoEstancia(planta.estancia))
                ivRiego.setImageResource(getIconoRiego(planta.riego))

                root.setOnClickListener { onItemClick(planta) }
            }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val binding = ItemPlantaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlantaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        holder.bind(plantas[position])
    }

    override fun getItemCount() = plantas.size

    fun actualizarLista(nuevaLista: List<ModelPlanta>) {
        plantas = nuevaLista
        notifyDataSetChanged()
    }
}