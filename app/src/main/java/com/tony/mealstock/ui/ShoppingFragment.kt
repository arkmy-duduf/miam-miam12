package com.tony.mealstock.ui

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

class ShoppingFragment: Fragment(){
  private lateinit var db:AppDb; private lateinit var dao:ShoppingDao; private lateinit var list:RecyclerView; private lateinit var adapter:ShoppingAdapter; private lateinit var edt:EditText
  override fun onCreateView(i:LayoutInflater,c:ViewGroup?,b:Bundle?):View{
    val v=i.inflate(R.layout.fragment_shopping,c,false)
    db=AppDb.get(requireContext()); dao=db.shoppingDao()
    edt=v.findViewById(R.id.edtNew); list=v.findViewById(R.id.list)
    list.layoutManager=LinearLayoutManager(requireContext()); adapter=ShoppingAdapter{ item-> viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ dao.update(item.copy(checked=!item.checked)) } }; list.adapter=adapter
    v.findViewById<Button>(R.id.btnAdd).setOnClickListener{ val name=edt.text.toString().trim(); if(name.isNotBlank()) viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ dao.insert(ShoppingItem(name=name)) }; edt.setText("") }
    v.findViewById<Button>(R.id.btnClear).setOnClickListener{ viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ dao.clear() } }
    dao.all().observe(viewLifecycleOwner, Observer{ items-> adapter.submit(items) })
    return v
  }
}
class ShoppingAdapter(val onToggle:(ShoppingItem)->Unit): RecyclerView.Adapter<ShoppingVH>(){
  private val items= mutableListOf<ShoppingItem>()
  fun submit(d:List<ShoppingItem>){ items.clear(); items.addAll(d); notifyDataSetChanged() }
  override fun onCreateViewHolder(p:ViewGroup,vt:Int)= ShoppingVH(LayoutInflater.from(p.context).inflate(R.layout.item_shopping,p,false))
  override fun getItemCount()=items.size
  override fun onBindViewHolder(h:ShoppingVH,i:Int)= h.bind(items[i], onToggle)
}
class ShoppingVH(v:View): RecyclerView.ViewHolder(v){
  private val chk:CheckBox=v.findViewById(R.id.chk); private val name:TextView=v.findViewById(R.id.name); private val qty:TextView=v.findViewById(R.id.qty)
  fun bind(it:ShoppingItem, onToggle:(ShoppingItem)->Unit){
    chk.isChecked=it.checked; name.text=it.name; qty.text= if(it.qty>0) "x${it.qty}" else ""
    chk.setOnCheckedChangeListener{ _, _ -> onToggle(it) }
  }
}
