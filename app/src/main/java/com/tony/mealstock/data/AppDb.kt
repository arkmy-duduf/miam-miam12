package com.tony.mealstock.data

import android.content.Context
import androidx.room.*

@Database(entities=[Product::class,Recipe::class,RecipeIngredient::class,MealPlan::class,ShoppingItem::class,ScanLog::class], version=1)
abstract class AppDb: RoomDatabase(){
  abstract fun productDao(): ProductDao
  abstract fun recipeDao(): RecipeDao
  abstract fun ingredientDao(): IngredientDao
  abstract fun planDao(): PlanDao
  abstract fun shoppingDao(): ShoppingDao
  abstract fun scanLogDao(): ScanLogDao

  companion object {
    @Volatile private var I: AppDb? = null
    fun get(ctx: Context): AppDb =
      I ?: synchronized(this) {
        I ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "mealstock.db").build().also { I = it }
      }
  }
}
