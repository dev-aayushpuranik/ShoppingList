package com.aayush.shoppingapp.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aayush.shoppingapp.database.dao.CategoryDao
import com.aayush.shoppingapp.database.entities.CategoryTable

@Database(entities = [CategoryTable::class], version = 2)
abstract class CategoryDatabase: RoomDatabase() {
    abstract fun CategoryDao(): CategoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: CategoryDatabase? = null

        fun getDatabase(context: Context): CategoryDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CategoryDatabase::class.java,
                    "CategoryDatabase"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}