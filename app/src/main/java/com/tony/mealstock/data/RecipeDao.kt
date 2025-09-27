package com.tony.mealstock.data

import androidx.room.*

data class RecipeWithItems(
  @Embedded val recipe: Recipe,
  @Relation(
    parentColumn = "id",
    entityColumn = "recipeId"
  )
  val items: List<RecipeItem>
)

@Dao
interface RecipeDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertRecipe(recipe: Recipe): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertItems(items: List<RecipeItem>)

  @Transaction
  @Query("SELECT * FROM recipes WHERE id=:id")
  suspend fun getRecipeWithItems(id: Long): RecipeWithItems?

  @Query("SELECT * FROM recipes ORDER BY name")
  suspend fun listRecipes(): List<Recipe>
}