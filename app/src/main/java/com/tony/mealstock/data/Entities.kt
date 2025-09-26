package com.tony.mealstock.data

import androidx.room.*

@Entity
data class Product(
  @PrimaryKey val barcode:String,
  val name:String="",
  val imageUrl:String="",
  val unit:String="piece",
  val parLevel:Double=1.0,
  val qty:Double=0.0
)

@Entity
data class Recipe(
  @PrimaryKey(autoGenerate=true) val id:Long=0,
  val title:String,
  val season:String?=null,
  val mealTime:String?=null,
  val servingsDefault:Int=4,
  val steps:String=""
)

@Entity(
  foreignKeys=[ForeignKey(entity=Recipe::class,parentColumns=["id"],childColumns=["recipeId"], onDelete=ForeignKey.CASCADE)],
  indices=[Index("recipeId")]
)
data class RecipeIngredient(
  @PrimaryKey(autoGenerate=true) val id:Long=0,
  val recipeId:Long,
  val name:String,
  val unit:String="piece",
  val qtyBase:Double,
  val barcode:String?=null
)

@Entity
data class MealPlan(
  @PrimaryKey(autoGenerate=true) val id:Long=0,
  val date:String,
  val mealTime:String,
  val recipeId:Long?=null,
  val servingsPlanned:Int=2
)

@Entity
data class ShoppingItem(
  @PrimaryKey(autoGenerate=true) val id:Long=0,
  val name:String,
  val barcode:String?=null,
  val qty:Double=1.0,
  val unit:String="piece",
  val checked:Boolean=false
)

@Entity
data class ScanLog(
  @PrimaryKey(autoGenerate=true) val id:Long=0,
  val barcode:String,
  val deltaQty:Double,
  val ts:Long=System.currentTimeMillis()
)
