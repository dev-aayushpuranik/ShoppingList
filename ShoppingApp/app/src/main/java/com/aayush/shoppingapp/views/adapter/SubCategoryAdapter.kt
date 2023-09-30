package com.aayush.shoppingapp.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefaut
import com.aayush.shoppingapp.databinding.SubcategoryRowViewBinding
import com.aayush.shoppingapp.models.SubCategoryListModel

class SubCategoryAdapter(
    private val context: Context,
    private val onImportantItemClick: (SubCategoryListModel) -> Unit,
    private val onTaskDoneIconClick: (SubCategoryListModel) -> Unit
) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }

    var data: List<SubCategoryListModel> = ArrayList(0)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class SubCategoryViewHolder(
        private val binding:SubcategoryRowViewBinding,
        onImportantItemClick: (Int) -> Unit,
        onTaskDoneIconClick: (Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.isImportantIv.setOnClickListener { onImportantItemClick(adapterPosition) }
            binding.isCompletedIv.setOnClickListener { onTaskDoneIconClick(adapterPosition) }
        }

        fun bind(context: Context, subCategoryModel: SubCategoryListModel) {
            binding.subCategoryTitleTv.text = subCategoryModel.subtaskName
            binding.subCategoryDescriptionTvTv.text = subCategoryModel.subtaskDescription
            binding.subCategoryDescriptionTvTv.SetViewVisible(subCategoryModel.subtaskDescription.isNotEmpty())

            val icon: Int =
                if (subCategoryModel.isTaskDone) R.drawable.ic_baseline_check_box_24
                else R.drawable.ic_baseline_check_box_outline_blank_24
            binding.isCompletedIv.setImageDrawable(ContextCompat.getDrawable(context, icon))

            val importantIcon: Int =
                if (subCategoryModel.isImportant) R.drawable.important_icon else R.drawable.unimportant_icon
            binding.isImportantIv.setImageDrawable(ContextCompat.getDrawable(context, importantIcon))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        if (viewType == VIEW_TYPE_ONE) {
            val binding = SubcategoryRowViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SubCategoryViewHolder(binding,
                { index ->
                    val item = data[index]
                    item.isImportant = !item.isImportant
                    onImportantItemClick(data[index])
                },
                { index ->
                    val item = data[index]
                    item.isTaskDone = !item.isTaskDone
                    onTaskDoneIconClick(item)
                })
        } else {
            val binding = SubcategoryRowViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SubCategoryViewHolder(binding,
                { index ->
                    val item = data[index]
                    item.isImportant = !item.isImportant
                    onImportantItemClick(data[index])
                },
                { index ->
                    val item = data[index]
                    item.isTaskDone = !item.isTaskDone
                    onTaskDoneIconClick(item)
                })
        }
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(context, data[position])
    }

    override fun getItemCount(): Int {
        return data.size.orDefaut()
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isTaskDone) {
            VIEW_TYPE_TWO
        } else VIEW_TYPE_ONE
    }
}