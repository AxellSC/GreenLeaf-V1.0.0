package com.example.greenleaf_v100.model

import android.widget.ImageView

data class ModelPlanta (
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val fotoUrl: String = "",
    val tipo: String = "",         // spnTipo
    val estancia: String = "",    // spnEstancia
    val riego: String = "",       // spnRiego
    val consejo: String = "",
)