package com.aayush.shoppingapp.views.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefault
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
        return CategoryViewHolder(parent.context, binding) { index ->
            onItemClick(data[index])
        }
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size.orDefault()
    }
}

class CategoryViewHolder(private val context: Context, private val binding: CategoryRowViewBinding, onItemClick: (Int) -> Unit):
    RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.setOnClickListener {
            onItemClick(adapterPosition)
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
        binding.categoryNameTV.text = model.CategoryName
        binding.DiscriptionNameTV.text = model.Description
        binding.DiscriptionNameTV.SetViewVisible(model.Description.isNotEmpty())

        val value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, context.resources.displayMetrics)
        binding.cardView.radius = value
        binding.cardView.preventCornerOverlap = true
        binding.cardView.useCompatPadding = true

        binding.cardView.background = ContextCompat.getDrawable(context, R.drawable.rounded_corners)
        binding.categoryNameTV.setTextColor(ContextCompat.getColor(context, R.color.app_text_color))
        binding.DiscriptionNameTV.setTextColor(ContextCompat.getColor(context, R.color.app_text_color))
    }
}