package com.aayush.shoppingapp.views.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.databinding.CategoryRowViewBinding
import com.aayush.shoppingapp.models.CategoryModel

class CategoryAdapter(
    private val onItemClick:(CategoryModel) -> Unit,
    private val onLongItemClicked:(CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryViewHolder>() {

    var data: List<CategoryModel> = ArrayList(0)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): CategoryViewHolder {
        val binding = CategoryRowViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(parent.context, binding, { index ->
            onItemClick(data[index])
        }, { index ->
            onLongItemClicked(data[index])
        })
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size.orDefault()
    }

    fun moveItem(from: Int, to: Int) {
        val fromItem = data[from]
        val toItem = data[to]
        val items = data as ArrayList
        items.removeAt(from)
        if (to < from) {
            items.add(to, fromItem)
        } else {
            items.add(to - 1, fromItem)
        }
    }
}

class CategoryViewHolder(private val context: Context, private val binding: CategoryRowViewBinding, onItemClick: (Int) -> Unit, onLongItemClicked: (Int) -> Unit):
    RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.setOnClickListener {
            onItemClick(adapterPosition)
        }
        itemView.setOnLongClickListener {
            onLongItemClicked(adapterPosition)
            true
        }
    }

    fun bind(model: CategoryModel) {
        if(model.CategoryId == 0L) {
            binding.imageView2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.important_icon))
            binding.imageView2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.impotant_color))
        } else {
            binding.imageView2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_format_list_bulleted_24))
            binding.imageView2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_text_color))
        }
        if(model.priorityId == PRIORITY.HIGH) {
            binding.priorityIcon.setColorFilter(context.getColor(android.R.color.holo_red_light))
        } else if (model.priorityId == PRIORITY.MEDIUM) {
            binding.priorityIcon.setColorFilter(context.getColor(android.R.color.holo_orange_light))
        } else if(model.priorityId == PRIORITY.LOW) {
            binding.priorityIcon.setColorFilter(context.getColor(android.R.color.holo_blue_dark))
        }
        binding.priorityIcon.setImageResource(R.drawable.priority_high_icon)
        binding.categoryNameTV.text = model.CategoryName
//        binding.DiscriptionNameTV.text = model.Description
//        binding.DiscriptionNameTV.SetViewVisible(model.Description.isNotEmpty())
    }
}