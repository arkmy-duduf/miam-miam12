package com.tony.mealstock.net

import retrofit2.http.GET
import retrofit2.http.Path
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class OFFResponse(val product: OFFProduct?)
data class OFFProduct(val product_name:String?, val image_url:String?)

interface OFFApi { @GET("api/v2/product/{code}.json") suspend fun getProduct(@Path("code") code:String): OFFResponse }

object OFF {
  private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
  private val client = OkHttpClient.Builder().addInterceptor(logging).build()
  val api: OFFApi = Retrofit.Builder().baseUrl("https://world.openfoodfacts.org/").client(client).addConverterFactory(GsonConverterFactory.create()).build().create(OFFApi::class.java)
}
