package com.aayush.shoppingapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SubcategoryTable(
    @PrimaryKey(autoGenerate = true)
    val subtaskItemId: Long,
    val categoryId: Long,
    val subCategoryName: String,
    val subCategoryDescription: String,
    val isTaskDone: Boolean,
    val isImportant: Boolean,
    val priorityId: Int
)