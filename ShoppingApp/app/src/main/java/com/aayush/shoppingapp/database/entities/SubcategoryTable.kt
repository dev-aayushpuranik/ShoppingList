package com.aayush.shoppingapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SubcategoryTable(
    @PrimaryKey(autoGenerate = true)
    val subCategoryId: Long,
    val subCategoryTitle: String,
    val subCategoryDescription: String,
    val subCategoryTotalItems: Int,
    val subCategoryPendingItems: Int,
    val categoryId: String,
)