package com.example.greenleaf_v100.DAO

import androidx.lifecycle.LiveData
import com.example.greenleaf_v100.model.CartItem

class CartRepository(private val dao: CartItemDao) {

    suspend fun add(item: CartItem) = dao.insert(item)
    suspend fun remove(item: CartItem) = dao.delete(item)

    fun getAllByUser(userId: String): LiveData<List<CartItem>> =
        dao.getAllByUser(userId)
}
