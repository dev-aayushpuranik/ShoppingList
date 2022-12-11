package com.aayush.shoppingapp.models

data class CategoryModel(
    val CategoryId: String,
    val CategoryName: String,
    val Description: String,
    val TotalItems: Int,
    val PendingItems: Int
)