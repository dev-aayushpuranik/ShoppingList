package com.aayush.shoppingapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefaut
import com.aayush.shoppingapp.models.CategoryModel

class CategoryAdapter(
    private val onItemClick:(CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryViewHolder>() {

    var data: List<CategoryModel> = ArrayList(0)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_row_view, parent, false)
        return CategoryViewHolder(view) { index ->
            onItemClick(data[index])
        }
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size.orDefaut()
    }
}

class CategoryViewHolder(view: View, onItemClick: (Int) -> Unit):
    RecyclerView.ViewHolder(view) {
    init {
        itemView.setOnClickListener {
            onItemClick(adapterPosition)
        }
    }

    fun bind(model: CategoryModel) {
        itemView.findViewById<TextView>(R.id.categoryNameTV).text = model.CategoryName
        itemView.findViewById<TextView>(R.id.DiscriptionNameTV).text = model.Description

        itemView.findViewById<TextView>(R.id.DiscriptionNameTV).SetViewVisible(model.Description.isNotEmpty())
    }
}