package com.tony.mealstock.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tony.mealstock.R
import com.tony.mealstock.data.AppDb
import com.tony.mealstock.data.ProductDao
import com.tony.mealstock.data.RecipeDao
import com.tony.mealstock.data.RecipeItem
import com.tony.mealstock.data.RecipeWithItems
import com.tony.mealstock.domain.RecipeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.ViewGroup.LayoutParams
import android.widget.TextView.BufferType

class RecipesFragment : Fragment() {

  private lateinit var db: AppDb
  private lateinit var pdao: ProductDao
  private lateinit var rdao: RecipeDao
  private lateinit var service: RecipeService

  private lateinit var tvTitle: TextView
  private lateinit var npServings: NumberPicker
  private lateinit var list: RecyclerView
  private lateinit var btnCook: Button

  private var current: RecipeWithItems? = null
  private var servings: Int = 2
  private val adapter = ItemsAdapter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val v = inflater.inflate(R.layout.fragment_recipes, container, false)
    db = AppDb.get(requireContext())
    pdao = db.productDao()
    rdao = db.recipeDao()
    service = RecipeService(db)

    tvTitle = v.findViewById(R.id.tvTitle)
    npServings = v.findViewById(R.id.npServings)
    list = v.findViewById(R.id.listItems)
    btnCook = v.findViewById(R.id.btnCook)

    npServings.minValue = 1
    npServings.maxValue = 6
    npServings.value = 2
    npServings.setOnValueChangedListener { _, _, newVal ->
      servings = newVal
      render()
    }

    list.layoutManager = LinearLayoutManager(requireContext())
    list.adapter = adapter

    btnCook.setOnClickListener {
      val rid = current?.recipe?.id ?: return@setOnClickListener
      viewLifecycleOwner.lifecycleScope.launch {
        val ok = service.cookRecipe(rid, servings)
        if (ok) Toast.makeText(requireContext(), "Stock mis a jour pour $servings personne(s)", Toast.LENGTH_SHORT).show()
        else Toast.makeText(requireContext(), "Recette introuvable", Toast.LENGTH_SHORT).show()
        render()
      }
    }

    viewLifecycleOwner.lifecycleScope.launch {
      val rid = service.createSampleIfEmpty()
      current = withContext(Dispatchers.IO) { rdao.getRecipeWithItems(rid) }
      tvTitle.text = current?.recipe?.name ?: "Recette"
      render()
    }

    return v
  }

  private fun render() {
    val bundle = current ?: return
    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
      val rows = bundle.items.map { it.toRow(servings, pdao) }
      withContext(Dispatchers.Main) { adapter.submit(rows) }
    }
  }

  data class Row(val barcode: String, val desc: String, val need: Double, val stock: Double)

  private fun RecipeItem.toRow(serv: Int, pdao: ProductDao): Row {
    val p = pdao.find(this.barcode)
    val need = this.qtyPerServing * serv
    val name = p?.name?.takeIf { it.isNotBlank() } ?: "(Produit $barcode)"
    val stock = p?.qty ?: 0.0
    return Row(barcode, name, need, stock)
  }

  private inner class ItemsAdapter : RecyclerView.Adapter<VH>() {
    private val data = mutableListOf<Row>()
    fun submit(rows: List<Row>) { data.clear(); data.addAll(rows); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
      val tv = TextView(parent.context)
      tv.setPadding(8, 12, 8, 12)
      tv.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
      return VH(tv)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
      val r = data[position]
      val ok = r.stock >= r.need
      val status = if (ok) "" else "  (manque ${"%.2f".format(r.need - r.stock)})"
      (holder.itemView as TextView).text =
        "${r.desc} — besoin ${"%.2f".format(r.need)} — stock ${"%.2f".format(r.stock)}$status"
    }
    override fun getItemCount(): Int = data.size
  }
  private class VH(v: View): RecyclerView.ViewHolder(v)
}