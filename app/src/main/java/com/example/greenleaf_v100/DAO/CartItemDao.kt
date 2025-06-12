package com.example.greenleaf_v100.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.greenleaf_v100.model.CartItem
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Delete

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getAllByUser(userId: String): LiveData<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: CartItem): Long

    @Delete
    fun delete(item: CartItem) : Int
}