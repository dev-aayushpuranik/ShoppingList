package com.aayush.shoppingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aayush.shoppingapp.database.dao.DatabaseDAO
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.database.entities.SubcategoryTable

@Database(entities = [CategoryTable::class, SubcategoryTable::class], version = 2)
abstract class ShoppingDatabase: RoomDatabase() {
    abstract fun databaseDAO(): DatabaseDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ShoppingDatabase? = null

        fun getDatabase(context: Context): ShoppingDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDatabase::class.java,
                    "ShoppingDatabase"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}