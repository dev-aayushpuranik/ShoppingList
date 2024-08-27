package com.aayush.shoppingapp.models

import com.aayush.shoppingapp.common.Enums.PRIORITY

data class SubCategoryListModel(
    val subtaskItemId: Long,
    val categoryId: Long,
    val subtaskName: String,
    val subtaskDescription: String,
    var isTaskDone: Boolean,
    var isImportant: Boolean,
    val priorityId: PRIORITY,
    var dueDate: Long?,
    val remindAt: Long?
)
