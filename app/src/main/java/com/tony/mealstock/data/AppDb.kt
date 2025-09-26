package com.tony.mealstock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class, ScanLog::class], version = 2, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun scanLogDao(): ScanLogDao

    companion object {
        @Volatile private var INSTANCE: AppDb? = null
        fun get(ctx: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "app.db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
