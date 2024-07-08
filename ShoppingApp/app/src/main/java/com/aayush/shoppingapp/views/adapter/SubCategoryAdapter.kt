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


class SubCategoryAdapter(
    private val context: Context,
    private val onImportantItemClick: (SubCategoryListModel) -> Unit,
    private val onTaskDoneIconClick: (SubCategoryListModel) -> Unit,
    private val onLongClickListener: (SubCategoryListModel) -> Unit
) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {

    var data: List<SubCategoryListModel> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class SubCategoryViewHolder(
        private val binding:SubcategoryRowViewBinding,
        onImportantItemClick: (Int) -> Unit,
        onTaskDoneIconClick: (Int) -> Unit,
        onLongClickListener: (Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.isImportantIv.setOnClickListener { onImportantItemClick(adapterPosition) }
            binding.isCompletedIv.setOnClickListener { onTaskDoneIconClick(adapterPosition) }
            binding.cardView.setOnClickListener { onLongClickListener(adapterPosition) }
        }

        fun bind(context: Context, subCategoryModel: SubCategoryListModel) {
            setDataForRow(subCategoryModel)

            binding.isCompletedIv.setImageDrawable(ContextCompat.getDrawable(context, getTaskIcon(subCategoryModel)))
            binding.isImportantIv.setImageDrawable(ContextCompat.getDrawable(context, getImportantIcon(subCategoryModel)))

            setColorForViewAndText(context)
        }

        private fun getImportantIcon(subCategoryModel: SubCategoryListModel):Int {
            return if (subCategoryModel.isImportant) R.drawable.important_icon else R.drawable.unimportant_icon
        }

        private fun getTaskIcon(subCategoryModel: SubCategoryListModel): Int {
            return if (subCategoryModel.isTaskDone) R.drawable.ic_baseline_check_box_24
            else R.drawable.ic_baseline_check_box_outline_blank_24
        }

        private fun setDataForRow(subCategoryModel: SubCategoryListModel) {
            binding.subCategoryTitleTv.text = subCategoryModel.subtaskName
            binding.subCategoryDescriptionTvTv.text = subCategoryModel.subtaskDescription
            binding.subCategoryDescriptionTvTv.SetViewVisible(subCategoryModel.subtaskDescription.isNotEmpty())
        }
        private fun setColorForViewAndText(context: Context) {
            binding.isCompletedIv.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.impotant_color))
            binding.subCategoryTitleTv.setTextColor(ContextCompat.getColor(context, R.color.app_text_color))
            binding.subCategoryDescriptionTvTv.setTextColor(ContextCompat.getColor(context, R.color.app_text_color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val binding =
            SubcategoryRowViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubCategoryViewHolder(binding,
            { index -> onImportantItemClicked(data[index]) },
            { index -> onDoneItemClicked(data[index]) },
            { index -> onLongClickListener(data[index])})
    }

    private fun onDoneItemClicked(item: SubCategoryListModel) {
        item.isTaskDone = !item.isTaskDone
        onTaskDoneIconClick(item)
    }

    private fun onImportantItemClicked(item: SubCategoryListModel) {
        item.isImportant = !item.isImportant
        onImportantItemClick(item)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(context, data[position])
    }

    override fun getItemCount(): Int {
        return data.size.orDefault()
    }
}