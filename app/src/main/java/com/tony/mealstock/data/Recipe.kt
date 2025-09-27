package com.tony.mealstock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val defaultServings: Int = 1
)