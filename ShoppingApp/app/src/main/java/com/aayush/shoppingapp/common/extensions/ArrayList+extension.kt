package com.aayush.shoppingapp.common.extensions

import java.util.*

// region List extension
fun <T>List<T>?.orDefault() : List<T> {
    return this ?: listOf<T>()
}
// endregion

// region ArrayList extensions
fun <T>ArrayList<T>?.orDefault() : ArrayList<T> {
    return this ?: arrayListOf<T>()
}
// endregion ArrayList extensions

// region Int extension

fun Int?.orDefaut(): Int{
    return this ?: 0
}

// endregion Int extension


// region String extension

fun String?.orDefaut(): String{
    return if(this != null && this.isNotEmpty()) this else ""
}

// endregion String extension
