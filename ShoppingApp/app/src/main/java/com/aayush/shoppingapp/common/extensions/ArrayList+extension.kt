package com.aayush.shoppingapp.common.extensions

import java.util.*

// region ArrayList extensions
fun <T>ArrayList<T>?.orDefault() : ArrayList<T> {
    return this ?: arrayListOf<T>()
}
// endregion ArrayList extensions

// region Int extension

fun Int?.orDefaut(): Int{
    return this ?: 0
}

// endregion
