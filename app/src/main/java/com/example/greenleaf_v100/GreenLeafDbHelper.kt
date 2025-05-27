package com.example.greenleaf_v100

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.greenleaf_v100.data.dao.FavoritoDao

private const val DATABASE_NAME = "GreenLeaf.db"
private const val DATABASE_VERSION = 1

class GreenLeafDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        // Tabla de favoritos (plantas favoritas)
        db.execSQL("""
            CREATE TABLE favoritos (
                id TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                descripcion TEXT,
                consejo TEXT,
                tipo TEXT,
                precio REAL,
                stock INTEGER,
                riego TEXT,
                estancia TEXT,
                fotoUrl TEXT
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS favoritos")
        onCreate(db)
    }

    fun getFavoritoDao(): FavoritoDao {
        return FavoritoDao(this.readableDatabase)
    }
}
