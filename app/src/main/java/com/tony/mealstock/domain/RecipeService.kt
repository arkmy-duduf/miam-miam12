package com.tony.mealstock.domain

import com.tony.mealstock.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeService(private val db: AppDb) {

  private val pdao = db.productDao()
  private val rdao = db.recipeDao()
  private val ldao = db.scanLogDao()

  suspend fun cookRecipe(recipeId: Long, servings: Int): Boolean = withContext(Dispatchers.IO) {
    val bundle = rdao.getRecipeWithItems(recipeId) ?: return@withContext false
    db.runInTransaction {
      bundle.items.forEach { item ->
        val need = item.qtyPerServing * servings
        val prod = pdao.find(item.barcode) ?: Product(barcode = item.barcode, name = "", qty = 0.0)
        val newQty = (prod.qty - need).coerceAtLeast(0.0)
        pdao.upsert(prod.copy(qty = newQty))
        ldao.insert(ScanLog(barcode = item.barcode, deltaQty = -need))
      }
    }
    true
  }

  suspend fun createSampleIfEmpty(): Long = withContext(Dispatchers.IO) {
    val existing = rdao.listRecipes()
    if (existing.isNotEmpty()) return@withContext existing.first().id
    val rid = rdao.upsertRecipe(Recipe(name = "Pates bolo", defaultServings = 2))
    rdao.upsertItems(
      listOf(
        RecipeItem(recipeId = rid, barcode = "8001040031005", qtyPerServing = 0.10), // 100g pates sec / pers
        RecipeItem(recipeId = rid, barcode = "3017620422003", qtyPerServing = 0.05)  // 50g sauce / pers (exemple)
      )
    )
    rid
  }
}