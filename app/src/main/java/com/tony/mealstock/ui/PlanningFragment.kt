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
import java.time.LocalDate

class PlanningFragment: Fragment(){
  private lateinit var db:AppDb; private lateinit var pdao:PlanDao; private lateinit var rdao:RecipeDao; private lateinit var idao:IngredientDao; private lateinit var prodao:ProductDao; private lateinit var sdao:ShoppingDao
  private lateinit var list:RecyclerView; private lateinit var adapter:PlanAdapter; private lateinit var txtWeek:TextView

  override fun onCreateView(i:LayoutInflater,c:ViewGroup?,b:Bundle?):View{
    val v=i.inflate(R.layout.fragment_planning,c,false)
    db=AppDb.get(requireContext()); pdao=db.planDao(); rdao=db.recipeDao(); idao=db.ingredientDao(); prodao=db.productDao(); sdao=db.shoppingDao()
    txtWeek=v.findViewById(R.id.txtWeek); list=v.findViewById(R.id.list)
    list.layoutManager=LinearLayoutManager(requireContext()); adapter=PlanAdapter({ pickRecipe(it) }, { changeServ(it) }, { validate(it) }); list.adapter=adapter

    val today=LocalDate.now()
    val start=today.minusDays(today.dayOfWeek.ordinal.toLong()); val end=start.plusDays(6)
    txtWeek.text="Semaine du ${start} → ${end}"

    pdao.between(start.toString(), end.toString()).observe(viewLifecycleOwner, Observer{ plans->
      val data= mutableListOf<MealPlan>(); var d=start
      repeat(7){
        data.add(plans.find{ it.date==d.toString() && it.mealTime=="midi" } ?: MealPlan(date=d.toString(), mealTime="midi"))
        data.add(plans.find{ it.date==d.toString() && it.mealTime=="soir" } ?: MealPlan(date=d.toString(), mealTime="soir"))
        d=d.plusDays(1)
      }
      adapter.submit(data)
    })
    return v
  }

  private fun pickRecipe(plan:MealPlan){
    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
      val rs=rdao.all().value?: emptyList()
      withContext(Dispatchers.Main){
        val titles=rs.map{ it.title }.toTypedArray()
        AlertDialog.Builder(requireContext()).setTitle("Choisir recette").setItems(titles){_,idx->
          val r=rs[idx]
          viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ pdao.upsert(plan.copy(recipeId=r.id, servingsPlanned=r.servingsDefault)) }
        }.show()
      }
    }
  }

  private fun changeServ(plan:MealPlan){
    val edt=EditText(requireContext()); edt.setText(plan.servingsPlanned.toString())
    AlertDialog.Builder(requireContext()).setTitle("Personnes").setView(edt).setPositiveButton("OK"){_,_->
      val s=edt.text.toString().toIntOrNull()?: plan.servingsPlanned
      viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ pdao.upsert(plan.copy(servingsPlanned=s)) }
    }.setNegativeButton("Annuler", null).show()
  }

  private fun validate(plan:MealPlan){
    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
      val rec = plan.recipeId?.let{ id-> rdao.all().value?.find{ it.id==id } }
      if(rec!=null){
        val ings=idao.forRecipe(rec.id)
        for(ing in ings){
          val need = ing.qtyBase * (plan.servingsPlanned.toDouble() / rec.servingsDefault.toDouble())
          if(ing.barcode!=null){
            val prod=prodao.find(ing.barcode); val avail=prod?.qty ?: 0.0; val deduct = kotlin.math.min(avail, need)
            if(prod==null){
              sdao.insert(ShoppingItem(name=ing.name, barcode=ing.barcode, qty=need, unit=ing.unit))
            } else {
              prodao.setQty(prod.barcode, avail - deduct)
              if(need - deduct > 0.0001) sdao.insert(ShoppingItem(name= if(prod.name.isBlank()) ing.name else prod.name, barcode=prod.barcode, qty=need-deduct, unit=ing.unit))
            }
          } else {
            sdao.insert(ShoppingItem(name=ing.name, qty=need, unit=ing.unit))
          }
        }
      }
    }
  }
}

class PlanAdapter(val onPick:(MealPlan)->Unit, val onServ:(MealPlan)->Unit, val onVal:(MealPlan)->Unit): RecyclerView.Adapter<PlanVH>(){
  private val items= mutableListOf<MealPlan>()
  fun submit(d:List<MealPlan>){ items.clear(); items.addAll(d); notifyDataSetChanged() }
  override fun onCreateViewHolder(p:ViewGroup,vt:Int)= PlanVH(LayoutInflater.from(p.context).inflate(R.layout.item_plan,p,false))
  override fun getItemCount()=items.size
  override fun onBindViewHolder(h:PlanVH,i:Int)= h.bind(items[i], onPick, onServ, onVal)
}

class PlanVH(v:View): RecyclerView.ViewHolder(v){
  private val date:TextView=v.findViewById(R.id.date)
  private val meal:TextView=v.findViewById(R.id.meal)
  private val recipe:TextView=v.findViewById(R.id.recipe)
  private val pick:Button=v.findViewById(R.id.btnPick)
  private val serv:Button=v.findViewById(R.id.btnServ)
  private val validate:Button=v.findViewById(R.id.btnValidate)
  fun bind(p:MealPlan, onPick:(MealPlan)->Unit, onServ:(MealPlan)->Unit, onVal:(MealPlan)->Unit){
    date.text=p.date; meal.text=p.mealTime; recipe.text="RecetteId: ${p.recipeId?: "—"}  Pers: ${p.servingsPlanned}"
    pick.setOnClickListener{ onPick(p) }; serv.setOnClickListener{ onServ(p) }; validate.setOnClickListener{ onVal(p) }
  }
}
