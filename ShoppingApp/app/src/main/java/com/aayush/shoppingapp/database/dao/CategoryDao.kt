package com.aayush.shoppingapp.database.dao

import androidx.room.*
import com.aayush.shoppingapp.database.entities.CategoryTable

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryTable: CategoryTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categoryList: List<CategoryTable>)

    @Delete
    suspend fun delete(categoryTable: CategoryTable)

    @Query("SELECT * FROM CategoryTable")
    suspend fun getAll(): List<CategoryTable>?

    @Update
    suspend fun updateCategory(categoryTable: CategoryTable)
}