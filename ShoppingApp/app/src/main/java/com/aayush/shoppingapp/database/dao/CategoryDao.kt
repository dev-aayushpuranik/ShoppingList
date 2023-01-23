package com.aayush.shoppingapp.database.dao

import androidx.room.*
import com.aayush.shoppingapp.database.entities.CategoryTable

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(categoryTable: CategoryTable)

    @Delete
    fun delete(categoryTable: CategoryTable)

    @Query("SELECT * FROM CategoryTable")
    fun getAll(): List<CategoryTable>?

    @Update
    fun updateCategory(categoryTable: CategoryTable)
}