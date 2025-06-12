package com.example.greenleaf_v100.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val price: Double,
    val imageUrl: String
)