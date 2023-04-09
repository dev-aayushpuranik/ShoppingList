package com.aayush.shoppingapp.common.extensions

import android.view.View

fun View.SetViewVisible(visible: Boolean) {
    this.visibility = if(visible) View.VISIBLE else View.GONE
}