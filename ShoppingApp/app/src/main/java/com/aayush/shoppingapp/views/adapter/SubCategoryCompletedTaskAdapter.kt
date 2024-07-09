package com.aayush.shoppingapp.views.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.databinding.SubcategoryRowViewBinding
import com.aayush.shoppingapp.models.SubCategoryListModel

class SubCategoryCompletedTaskAdapter(
    private val context: Context,
    private val onImportantItemClick: (SubCategoryListModel) -> Unit,
    private val onTaskDoneIconClick: (SubCategoryListModel) -> Unit
) : RecyclerView.Adapter<SubCategoryCompletedTaskAdapter.SubCategoryCompletedTaskViewHolder>() {

    var data: List<SubCategoryListModel> = ArrayList(0)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class SubCategoryCompletedTaskViewHolder(
        private val binding: SubcategoryRowViewBinding,
        onImportantItemClick: (Int) -> Unit,
        onTaskDoneIconClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.isImportantIv.setOnClickListener { onImportantItemClick(adapterPosition) }
            binding.isCompletedIv.setOnClickListener { onTaskDoneIconClick(adapterPosition) }
        }

        fun bind(context: Context, subCategoryModel: SubCategoryListModel) {
            setDataForRow(subCategoryModel)

            val icon: Int =
                if (subCategoryModel.isTaskDone) R.drawable.ic_baseline_check_box_24
                else R.drawable.ic_baseline_check_box_outline_blank_24
            binding.isCompletedIv.setImageDrawable(ContextCompat.getDrawable(context, icon))

            val importantIcon: Int =
                if (subCategoryModel.isImportant) R.drawable.important_icon else R.drawable.unimportant_icon
            binding.isImportantIv.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    importantIcon
                )
            )

            setColorForViewAndText(context)
        }

        private fun setDataForRow(subCategoryModel: SubCategoryListModel) {
            binding.subCategoryTitleTv.text = subCategoryModel.subtaskName
            binding.subCategoryDescriptionTvTv.text = subCategoryModel.subtaskDescription
            binding.subCategoryDescriptionTvTv.SetViewVisible(subCategoryModel.subtaskDescription.isNotEmpty())
        }

        private fun setColorForViewAndText(context: Context) {
            binding.isCompletedIv.imageTintList =
                ColorStateList.valueOf(getColor(context, R.color.app_text_color))
            binding.subCategoryTitleTv.setTextColor(
                getColor(context, R.color.app_text_color)
            )
            binding.subCategoryDescriptionTvTv.setTextColor(
                getColor(context, R.color.app_text_color)
            )
        }

        private fun getColor(context: Context, color: Int): Int {
            return ContextCompat.getColor(context, color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryCompletedTaskViewHolder {
        val binding = SubcategoryRowViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SubCategoryCompletedTaskViewHolder(binding,
            { index -> onImportantItemClicked(data[index]) },
            { index -> onDoneItemClicked(data[index]) })
    }

    override fun onBindViewHolder(holder: SubCategoryCompletedTaskViewHolder, position: Int) {
        holder.bind(context, data[position])
    }

    private fun onDoneItemClicked(item: SubCategoryListModel) {
        item.isTaskDone = !item.isTaskDone
        onTaskDoneIconClick(item)
    }

    private fun onImportantItemClicked(item: SubCategoryListModel) {
        onImportantItemClick(item)
    }

    override fun getItemCount(): Int {
        return data.size.orDefault()
    }
}