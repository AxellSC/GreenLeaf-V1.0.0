package com.example.greenleaf_v100.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.greenleaf_v100.model.ModelPlanta

class FavoritoDao(private val db: SQLiteDatabase) {

    fun agregarFavorito(planta: ModelPlanta) {
        val cv = ContentValues().apply {
            put("id", planta.id)
            put("nombre", planta.nombre)
            put("descripcion", planta.descripcion)
            put("consejo", planta.consejo)
            put("tipo", planta.tipo)
            put("precio", planta.precio)
            put("stock", planta.stock)
            put("riego", planta.riego)
            put("estancia", planta.estancia)
            put("fotoUrl", planta.fotoUrl)
        }
        db.insertWithOnConflict("favoritos", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun eliminarFavorito(id: String) {
        db.delete("favoritos", "id = ?", arrayOf(id))
    }

    fun esFavorito(id: String): Boolean {
        val cursor = db.rawQuery("SELECT 1 FROM favoritos WHERE id = ?", arrayOf(id))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun obtenerFavoritos(): List<ModelPlanta> {
        val lista = mutableListOf<ModelPlanta>()
        val cursor = db.rawQuery("SELECT * FROM favoritos", null)
        cursor.use {
            while (it.moveToNext()) {
                lista += ModelPlanta(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                    descripcion = it.getString(it.getColumnIndexOrThrow("descripcion")),
                    consejo = it.getString(it.getColumnIndexOrThrow("consejo")),
                    tipo = it.getString(it.getColumnIndexOrThrow("tipo")),
                    precio = it.getString(it.getColumnIndexOrThrow("precio")),
                    stock = it.getInt(it.getColumnIndexOrThrow("stock")),
                    riego = it.getString(it.getColumnIndexOrThrow("riego")),
                    estancia = it.getString(it.getColumnIndexOrThrow("estancia")),
                    fotoUrl = it.getString(it.getColumnIndexOrThrow("fotoUrl"))
                )
            }
        }
        return lista
    }
}
