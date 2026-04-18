package com.aayush.shoppingapp.models

import android.os.Parcelable
import com.aayush.shoppingapp.common.Enums.PRIORITY
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryModel(
    val CategoryId: Long,
    val CategoryName: String,
    val Description: String,
    val priorityId: PRIORITY
) : Parcelable