package com.tony.mealstock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [Product::class, ScanLog::class, Recipe::class, RecipeItem::class],
  version = 3,
  exportSchema = false
)
abstract class AppDb : RoomDatabase() {
  abstract fun productDao(): ProductDao
  abstract fun scanLogDao(): ScanLogDao
  abstract fun recipeDao(): RecipeDao

  companion object {
    @Volatile private var INSTANCE: AppDb? = null
    fun get(ctx: Context): AppDb =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "app.db")
          .fallbackToDestructiveMigration() // simple pour avancer
          .build().also { INSTANCE = it }
      }
  }
}