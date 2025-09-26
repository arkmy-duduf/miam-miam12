package com.tony.mealstock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
  @PrimaryKey val barcode: String,
  val name: String = "",
  val qty: Double = 0.0,
  val imageUrl: String? = null
)
