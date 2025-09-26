package com.tony.mealstock.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.tony.mealstock.R

class SettingsFragment: Fragment(){
  override fun onCreateView(i:LayoutInflater,c:ViewGroup?,b:Bundle?):View{
    val v=i.inflate(R.layout.fragment_settings,c,false)
    val prefs=requireContext().getSharedPreferences("settings", 0)
    val par=v.findViewById<EditText>(R.id.edtParDefault)
    val days=v.findViewById<EditText>(R.id.edtDaysAlert)
    val serv=v.findViewById<EditText>(R.id.edtServDefault)
    val feas=v.findViewById<EditText>(R.id.edtRecipeFeasible)
    par.setText(prefs.getFloat("parDefault",1f).toString())
    days.setText(prefs.getInt("daysAlert",3).toString())
    serv.setText(prefs.getInt("servDefault",4).toString())
    feas.setText(prefs.getInt("feasiblePct",70).toString())
    v.findViewById<Button>(R.id.btnSave).setOnClickListener{
      prefs.edit().putFloat("parDefault", par.text.toString().toFloatOrNull()?:1f)
        .putInt("daysAlert", days.text.toString().toIntOrNull()?:3)
        .putInt("servDefault", serv.text.toString().toIntOrNull()?:4)
        .putInt("feasiblePct", feas.text.toString().toIntOrNull()?:70)
        .apply()
    }
    return v
  }
}
