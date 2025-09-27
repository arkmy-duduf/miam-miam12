package com.tony.mealstock.ui
import android.content.Intent
import com.tony.mealstock.ui.RecipesActivity
import android.view.Menu

import android.view.MenuItem
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tony.mealstock.R

class MainActivity: AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
    fun switch(f: Fragment) { supportFragmentManager.beginTransaction().replace(R.id.container, f).commit() }
    nav.setOnItemSelectedListener {
      when (it.itemId) {
        R.id.nav_scanner -> switch(ScannerFragment())
        R.id.nav_stock -> switch(StockFragment())
        R.id.nav_myrecipes -> switch(MyRecipesFragment())
        R.id.nav_planning -> switch(PlanningFragment())
        R.id.nav_shopping -> switch(ShoppingFragment())

      }; true
    }
    nav.selectedItemId = R.id.nav_scanner
  }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_recipes -> {
                startActivity(Intent(this, RecipesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

