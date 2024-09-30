package com.aayush.shoppingapp.database.dao

import androidx.room.*
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.database.entities.SubcategoryTable

@Dao
interface DatabaseDAO {
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subcategoryTable: SubcategoryTable)

    @Insert
    fun insertAll(list: List<SubcategoryTable>)

    @Delete
    fun delete(subcategoryTable: SubcategoryTable)

    @Query("SELECT * FROM SubcategoryTable where categoryId=:catId")
    fun getAll(catId: Long): List<SubcategoryTable>?

    @Query("SELECT * FROM SubcategoryTable")
    fun getAllSubCategories(): List<SubcategoryTable>?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTaskDoneForSubCategory(subcategoryTable: SubcategoryTable)
}