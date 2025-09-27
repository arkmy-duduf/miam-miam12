package com.tony.mealstock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "recipe_items",
  foreignKeys = [
    ForeignKey(
      entity = Recipe::class,
      parentColumns = ["id"],
      childColumns = ["recipeId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("recipeId"), Index("barcode")]
)
data class RecipeItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val recipeId: Long,
  val barcode: String,      // référence Product.barcode
  val qtyPerServing: Double // quantité nécessaire / personne
)