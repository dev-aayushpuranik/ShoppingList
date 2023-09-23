package com.aayush.shoppingapp.views.adapter

import android.content.ClipData.Item
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefaut
import com.aayush.shoppingapp.models.SubCategoryListModel

class SubCategoryAdapter(
    private val context: Context,
    private val onItemClick:(SubCategoryListModel) -> Unit,
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

    class SubCategoryViewHolder(view: View,
                                onItemClick: (Int) -> Unit,
                                onTaskDoneIconClick:(Int) -> Unit):
        RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener { onItemClick(adapterPosition) }
            itemView.findViewById<ImageView>(R.id.is_completed_iv).setOnClickListener { onTaskDoneIconClick(adapterPosition) }
        }

        fun bind(context: Context, subCategoryModel: SubCategoryListModel) {
            itemView.findViewById<TextView>(R.id.subCategoryTitleTv).text = subCategoryModel.subtaskName
            (itemView.findViewById<TextView>(R.id.subCategoryDescriptionTvTv))
                .SetViewVisible(subCategoryModel.subtaskDescription.isNotEmpty())

            itemView.findViewById<TextView>(R.id.subCategoryDescriptionTvTv).text =
                subCategoryModel.subtaskDescription

            val icon: Int = if(subCategoryModel.isTaskDone) R.drawable.ic_baseline_check_box_24 else R.drawable.ic_baseline_check_box_outline_blank_24

            val taskCompleted:ImageView = itemView.findViewById(R.id.is_completed_iv)
            taskCompleted.setImageDrawable(ContextCompat.getDrawable(context, icon))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        if(viewType == VIEW_TYPE_ONE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.subcategory_row_view, parent, false)
            return SubCategoryViewHolder(view,
                { index -> onItemClick(data[index]) },
                { index ->
                    val item = data[index]
                    item.isTaskDone = !item.isTaskDone
                    onTaskDoneIconClick(item)
                })
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.subcategory_row_task_completed_view, parent, false)

            return SubCategoryViewHolder(view,
                { index -> onItemClick(data[index]) },
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
        return if(data[position].isTaskDone) { VIEW_TYPE_TWO } else VIEW_TYPE_ONE
    }
}