package com.aayush.shoppingapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val CategoryTitle: String,
    val CategoryDescription: String
)