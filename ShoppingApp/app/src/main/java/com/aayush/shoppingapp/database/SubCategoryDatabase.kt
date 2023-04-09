package com.aayush.shoppingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aayush.shoppingapp.database.dao.SubcategoryDao
import com.aayush.shoppingapp.database.entities.SubcategoryTable

@Database(entities = [SubcategoryTable::class], version = 1)
abstract class SubCategoryDatabase : RoomDatabase() {

    abstract fun SubcategoryDao(): SubcategoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: SubCategoryDatabase? = null

        fun getDatabase(context: Context): SubCategoryDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubCategoryDatabase::class.java,
                    "SubCategoryDatabase"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}