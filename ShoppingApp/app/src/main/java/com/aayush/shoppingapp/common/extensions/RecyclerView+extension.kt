package com.aayush.shoppingapp.common.extensions

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun <T>RecyclerView.setAdapterWithLinearLayout(
    context: Context,
    adapter: T
) {
    this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    this.adapter = adapter as RecyclerView.Adapter<*>
}

fun <T>RecyclerView.setAdapter(
    linearLayoutManager: LinearLayoutManager,
    adapter: T
) {
    this.layoutManager = linearLayoutManager
    this.adapter = adapter as RecyclerView.Adapter<*>
}