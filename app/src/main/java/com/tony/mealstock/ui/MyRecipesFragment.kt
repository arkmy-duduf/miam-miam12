package com.tony.mealstock.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.tony.mealstock.R
import com.tony.mealstock.data.*
import kotlinx.coroutines.*

class MyRecipesFragment: Fragment(){
  private lateinit var db:AppDb; private lateinit var rdao:RecipeDao; private lateinit var idao:IngredientDao; private lateinit var list:RecyclerView; private lateinit var adapter:RecipeAdapter

  override fun onCreateView(i:LayoutInflater,c:ViewGroup?,b:Bundle?):View{
    val v=i.inflate(R.layout.fragment_myrecipes,c,false)
    db=AppDb.get(requireContext()); rdao=db.recipeDao(); idao=db.ingredientDao()
    list=v.findViewById(R.id.list); list.layoutManager=LinearLayoutManager(requireContext())
    adapter=RecipeAdapter({},{ r-> editRecipe(r) }); list.adapter=adapter
    rdao.all().observe(viewLifecycleOwner, Observer{ rs-> adapter.submit(rs) })
    v.findViewById<Button>(R.id.btnNew).setOnClickListener{ editRecipe(null) }
    return v
  }

  private fun editRecipe(recipe:Recipe?){
    val dlgV=layoutInflater.inflate(R.layout.dialog_recipe_edit, null)
    val t=dlgV.findViewById<EditText>(R.id.edtTitle)
    val s=dlgV.findViewById<EditText>(R.id.edtServings)
    val sea=dlgV.findViewById<EditText>(R.id.edtSeason)
    val m=dlgV.findViewById<EditText>(R.id.edtMealTime)
    val ings=dlgV.findViewById<EditText>(R.id.edtIngredients)
    val steps=dlgV.findViewById<EditText>(R.id.edtSteps)

    recipe?.let{
      t.setText(it.title); s.setText(it.servingsDefault.toString()); sea.setText(it.season?: ""); m.setText(it.mealTime?: "")
      viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
        val L = idao.forRecipe(it.id)
        val txt = L.joinToString("\n"){ i-> "${i.name}|${i.qtyBase}|${i.unit}|${i.barcode?: ""}" }
        withContext(Dispatchers.Main){ ings.setText(txt) }
      }
      steps.setText(it.steps)
    }

    val dlg=AlertDialog.Builder(requireContext()).setView(dlgV).create()
    dlgV.findViewById<Button>(R.id.btnCancel).setOnClickListener{ dlg.dismiss() }
    dlgV.findViewById<Button>(R.id.btnSave).setOnClickListener{
      val title=t.text.toString().ifBlank{ "Recette" }
      val serv=s.text.toString().toIntOrNull()?:4
      val season=sea.text.toString().ifBlank{ null }
      val meal=m.text.toString().ifBlank{ null }
      val st=steps.text.toString()

      viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
        val id = if (recipe==null) rdao.insert(Recipe(title=title, servingsDefault=serv, season=season, mealTime=meal, steps=st))
                 else { rdao.update(recipe.copy(title=title, servingsDefault=serv, season=season, mealTime=meal, steps=st)); recipe.id }
        idao.clearForRecipe(id)
        val lines = ings.text.toString().split("\n").mapNotNull { line->
          val parts = line.split("|")
          if (parts.size>=3) {
            val name=parts[0].trim(); val q=parts[1].trim().toDoubleOrNull()?:0.0; val u=parts[2].trim().ifBlank{"piece"}; val bc=parts.getOrNull(3)?.trim().takeUnless{ it.isNullOrBlank() }
            RecipeIngredient(recipeId=id, name=name, qtyBase=q, unit=u, barcode=bc)
          } else null
        }
        if (lines.isNotEmpty()) idao.insertAll(lines)
      }
      dlg.dismiss()
    }
    dlg.show()
  }
}

class RecipeAdapter(val onPlan:(Recipe)->Unit, val onEdit:(Recipe)->Unit): RecyclerView.Adapter<RecipeVH>(){
  private val items= mutableListOf<Recipe>()
  fun submit(d:List<Recipe>){ items.clear(); items.addAll(d); notifyDataSetChanged() }
  override fun onCreateViewHolder(p:ViewGroup,vt:Int)= RecipeVH(LayoutInflater.from(p.context).inflate(R.layout.item_recipe,p,false))
  override fun getItemCount()=items.size
  override fun onBindViewHolder(h:RecipeVH,i:Int)= h.bind(items[i], onPlan, onEdit)
}

class RecipeVH(v:View): RecyclerView.ViewHolder(v){
  private val title:TextView=v.findViewById(R.id.title)
  private val meta:TextView=v.findViewById(R.id.meta)
  private val btnPlan:Button=v.findViewById(R.id.btnPlan)
  private val btnEdit:Button=v.findViewById(R.id.btnEdit)
  fun bind(r:Recipe, onPlan:(Recipe)->Unit, onEdit:(Recipe)->Unit){
    title.text=r.title
    meta.text="${r.season ?: "—"} ${r.mealTime ?: "—"} (${r.servingsDefault} pers)"
    btnPlan.setOnClickListener{ onPlan(r) }
    btnEdit.setOnClickListener{ onEdit(r) }
  }
}
