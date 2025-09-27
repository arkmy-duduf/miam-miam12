package com.tony.mealstock.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tony.mealstock.R

class RecipesActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_recipes)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.recipes_container, RecipesFragment())
        .commit()
    }
  }
}