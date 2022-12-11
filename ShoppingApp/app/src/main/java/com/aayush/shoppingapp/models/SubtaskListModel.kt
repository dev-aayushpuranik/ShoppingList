package com.aayush.shoppingapp.models

data class SubtaskListModel(
    val categoryId: String,
    val subtaskItemId: String,
    val subtaskName: String,
    val subtaskDescription: String,
    val isTaskDone: Boolean,
    val isImportant: Boolean
)
