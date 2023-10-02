package com.aayush.shoppingapp.models

data class SubCategoryListModel(
    val subtaskItemId: Long,
    val categoryId: Long,
    val subtaskName: String,
    val subtaskDescription: String,
    var isTaskDone: Boolean,
    var isImportant: Boolean
)
