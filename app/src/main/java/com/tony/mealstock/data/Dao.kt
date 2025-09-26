package com.tony.mealstock.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao {
  @Query("SELECT * FROM Product ORDER BY name")
  fun all(): LiveData<List<Product>>

  @Query("SELECT * FROM Product WHERE barcode=:barcode LIMIT 1")
  suspend fun find(barcode:String): Product?

  @Insert(onConflict=OnConflictStrategy.REPLACE)
  suspend fun upsert(p:Product)

  @Query("UPDATE Product SET qty=qty + :delta WHERE barcode=:barcode")
  suspend fun addQty(barcode:String, delta:Double)

  @Query("UPDATE Product SET qty=:qty WHERE barcode=:barcode")
  suspend fun setQty(barcode:String, qty:Double)
}

@Dao
interface RecipeDao {
  @Query("SELECT * FROM Recipe ORDER BY title")
  fun all(): LiveData<List<Recipe>>
  @Insert suspend fun insert(r:Recipe): Long
  @Update suspend fun update(r:Recipe)
  @Delete suspend fun delete(r:Recipe)
}

@Dao
interface IngredientDao {
  @Query("SELECT * FROM RecipeIngredient WHERE recipeId=:recipeId")
  suspend fun forRecipe(recipeId:Long): List<RecipeIngredient>
  @Insert suspend fun insertAll(list:List<RecipeIngredient>)
  @Query("DELETE FROM RecipeIngredient WHERE recipeId=:recipeId")
  suspend fun clearForRecipe(recipeId:Long)
}

@Dao
interface PlanDao {
  @Query("SELECT * FROM MealPlan WHERE date BETWEEN :start AND :end ORDER BY date, mealTime")
  fun between(start:String, end:String): LiveData<List<MealPlan>>
  @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(p:MealPlan)
}

@Dao
interface ShoppingDao {
  @Query("SELECT * FROM ShoppingItem ORDER BY checked, name")
  fun all(): LiveData<List<ShoppingItem>>
  @Insert suspend fun insert(i:ShoppingItem)
  @Update suspend fun update(i:ShoppingItem)
  @Query("DELETE FROM ShoppingItem") suspend fun clear()
}

@Dao
interface ScanLogDao { @Insert suspend fun insert(l:ScanLog) }
