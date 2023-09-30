package com.aayush.shoppingapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefaut
import com.aayush.shoppingapp.databinding.CategoryRowViewBinding
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
        val binding = CategoryRowViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding) { index ->
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

class CategoryViewHolder(private val binding: CategoryRowViewBinding, onItemClick: (Int) -> Unit):
    RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.setOnClickListener {
            onItemClick(adapterPosition)
        }
    }

    fun bind(model: CategoryModel) {
        binding.categoryNameTV.text = model.CategoryName
        binding.DiscriptionNameTV.text = model.Description
        binding.DiscriptionNameTV.SetViewVisible(model.Description.isNotEmpty())
    }
}