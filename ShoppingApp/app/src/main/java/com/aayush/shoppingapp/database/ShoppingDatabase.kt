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
}