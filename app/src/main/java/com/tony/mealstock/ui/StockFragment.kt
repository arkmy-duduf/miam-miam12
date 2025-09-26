package com.tony.mealstock.ui

import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.tony.mealstock.R
import com.tony.mealstock.data.*
import kotlinx.coroutines.*

class StockFragment: Fragment(){
  private lateinit var db:AppDb; private lateinit var dao:ProductDao; private lateinit var list:RecyclerView; private lateinit var adapter:StockAdapter; private lateinit var search:EditText
  override fun onCreateView(i:LayoutInflater,c:ViewGroup?,b:Bundle?):View{
    val v=i.inflate(R.layout.fragment_stock,c,false)
    db=AppDb.get(requireContext()); dao=db.productDao()
    search=v.findViewById(R.id.search); list=v.findViewById(R.id.list)
    list.layoutManager=LinearLayoutManager(requireContext())
    adapter=StockAdapter({bc-> viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ dao.addQty(bc, +1.0) } }, {bc-> viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ dao.addQty(bc, -1.0) } })
    list.adapter=adapter
    val obs=Observer<List<Product>>{ data-> val q=search.text.toString().lowercase(); adapter.submit(data.filter{ it.name.lowercase().contains(q) || it.barcode.contains(q) }) }
    dao.all().observe(viewLifecycleOwner, obs)
    search.addTextChangedListener(object: TextWatcher{
      override fun afterTextChanged(s:Editable?){} override fun beforeTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){} 
      override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){ dao.all().value?.let{ data-> val q=(s?:"").toString().lowercase(); adapter.submit(data.filter{ it.name.lowercase().contains(q) || it.barcode.contains(q) }) } }
    })
    return v
  }
}

class StockAdapter(val onPlus:(String)->Unit, val onMinus:(String)->Unit): RecyclerView.Adapter<StockVH>(){
  private val items= mutableListOf<Product>()
  fun submit(d:List<Product>){ items.clear(); items.addAll(d); notifyDataSetChanged() }
  override fun onCreateViewHolder(p:ViewGroup,vt:Int)= StockVH(LayoutInflater.from(p.context).inflate(R.layout.item_stock,p,false))
  override fun getItemCount()=items.size
  override fun onBindViewHolder(h:StockVH,i:Int)= h.bind(items[i], onPlus, onMinus)
}

class StockVH(v:View): RecyclerView.ViewHolder(v){
  private val img:ImageView=v.findViewById(R.id.img); private val name:TextView=v.findViewById(R.id.name); private val barcode:TextView=v.findViewById(R.id.barcode); private val qty:TextView=v.findViewById(R.id.qty); private val plus:Button=v.findViewById(R.id.plus); private val minus:Button=v.findViewById(R.id.minus)
  fun bind(p:Product, onPlus:(String)->Unit, onMinus:(String)->Unit){
    name.text= if(p.name.isBlank())"(sans nom)" else p.name
    barcode.text=p.barcode
    qty.text="x${p.qty.toInt()}"
    if(p.imageUrl.isNotBlank()) Glide.with(itemView).load(p.imageUrl).into(img) else img.setImageDrawable(null)
    plus.setOnClickListener{ onPlus(p.barcode) }
    minus.setOnClickListener{ onMinus(p.barcode) }
  }
}
