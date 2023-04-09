package com.aayush.shoppingapp.database.dao

import androidx.room.*
import com.aayush.shoppingapp.database.entities.SubcategoryTable

@Dao
interface SubcategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subcategoryTable: SubcategoryTable)

    @Insert
    fun insertAll(list: List<SubcategoryTable>?)

    @Delete
    fun delete(subcategoryTable: SubcategoryTable)

    @Query("SELECT * FROM SubcategoryTable")
    fun getAll(): List<SubcategoryTable>?

    @Update
    fun updateCategory(subcategoryTable: SubcategoryTable)
}