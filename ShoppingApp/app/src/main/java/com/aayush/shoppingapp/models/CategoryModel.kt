package com.aayush.shoppingapp.models

import com.aayush.shoppingapp.common.Enums.PRIORITY

data class CategoryModel(
    val CategoryId: Long,
    val CategoryName: String,
    val Description: String,
    val priorityId: PRIORITY
)