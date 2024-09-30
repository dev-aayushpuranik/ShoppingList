package com.aayush.shoppingapp.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.databinding.FragmentEditBinding
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.viewModels.CategoriesViewModel
import com.aayush.shoppingapp.viewModels.SubCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditFragment(private val subCategoryListModel: SubCategoryListModel? = null) : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private var mCategoryModel: CategoryModel? = null
    private lateinit var subCategoryViewModel: SubCategoryViewModel
    private lateinit var categoryViewModel: CategoriesViewModel

    constructor(categoryModel: CategoryModel): this(null) {
        this.mCategoryModel = categoryModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        subCategoryViewModel = ViewModelProvider(this)[SubCategoryViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]

        val title = subCategoryListModel?.subtaskName ?:  mCategoryModel?.CategoryName ?: ""
        val descripton = subCategoryListModel?.subtaskDescription ?: ""

        binding.toolbar.toolbarTitle.text = "Edit $title"
        binding.nameTxtVw.text = Editable.Factory.getInstance().newEditable(title)
        binding.descriptionTxtVw.text = Editable.Factory.getInstance().newEditable(descripton)
        binding.toolbar.saveBtn.SetViewVisible(true)

        if(mCategoryModel != null) {
            binding.isImportantIv.SetViewVisible(false)
            binding.descriptionTxtVw.SetViewVisible(false)
        }
        if(subCategoryListModel != null) {
            binding.isImportantIv.SetViewVisible(true)
            binding.descriptionTxtVw.SetViewVisible(true)
            setImportantIcon()
            binding.isImportantIv.setOnClickListener {
                subCategoryListModel.isImportant = !(subCategoryListModel.isImportant)
                setImportantIcon()
            }
        }

        binding.toolbar.toolbarBackArrow.setOnClickListener {
            if(isValidated()) {
                parentFragmentManager.popBackStack()
            } else {
                UIHelper.showAlertDialog(requireContext(), "Unsaved Data", "There is unsaved Data. Please save before leaving", {})
            }
        }

        val arrayAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_level,
            android.R.layout.simple_list_item_1
        )
        binding.prioritySelector.adapter = arrayAdapter
        if(subCategoryListModel != null) {
            binding.prioritySelector.setSelection(
                subCategoryListModel.priorityId.value - 1, false)
        } else if(mCategoryModel != null) {
            binding.prioritySelector.setSelection(
                (mCategoryModel?.priorityId?.value ?: PRIORITY.LOW.value) - 1, false)
        }

        binding.toolbar.saveBtn.setOnClickListener {
            if(mCategoryModel != null) {
                onCategorySaveBtnClicked()
            } else if(subCategoryListModel != null) {
                onSubcategorySaveBtnClicked()
            }
        }

        return binding.root
    }

    private fun onCategorySaveBtnClicked() {
        mCategoryModel?.let {
            val categoryModel = CategoryModel(
                it.CategoryId,
                binding.nameTxtVw.text.toString().trim(),
                binding.descriptionTxtVw.text.toString().trim(),
                getSelectedPriorityForTask()
            )
            lifecycleScope.launch {
                categoryViewModel.updateCategory(requireContext(), categoryModel)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun onSubcategorySaveBtnClicked() {
        if(subCategoryListModel == null) {return}
        val subCategoryListModel = SubCategoryListModel(
            subCategoryListModel.subtaskItemId, subCategoryListModel.categoryId,
            binding.nameTxtVw.text.toString().trim(),
            binding.descriptionTxtVw.text.toString().trim(),
            subCategoryListModel.isTaskDone, subCategoryListModel.isImportant,
            getSelectedPriorityForTask(), subCategoryListModel.dueDate, subCategoryListModel.remindAt
        )

        subCategoryViewModel.updateCategoryItem(requireContext(), subCategoryListModel, {
            parentFragmentManager.popBackStack()
        }, {
            UIHelper.showAlertDialog(requireContext(), "", "", {})
        })
    }

    private fun isValidated(): Boolean {
        return (subCategoryListModel != null && (subCategoryListModel.subtaskName == binding.nameTxtVw.text.toString() &&
                subCategoryListModel.subtaskDescription == binding.descriptionTxtVw.text.toString() &&
                subCategoryListModel.priorityId == getSelectedPriorityForTask()))
    }

    private fun getSelectedPriorityForTask(): PRIORITY {
        val priorityIndex = binding.prioritySelector.selectedItemPosition
        return if (priorityIndex + 1 == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex + 1 == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
    }

    private fun setImportantIcon() {
        if(subCategoryListModel?.isImportant == true) {
            binding.isImportantIv.setImageDrawable(returnDrawable(R.drawable.important_icon))
        } else {
            binding.isImportantIv.setImageDrawable(returnDrawable(R.drawable.unimportant_icon))
        }
    }

    private fun returnDrawable(icon: Int) : Drawable? {
        return ContextCompat.getDrawable(requireContext(), icon)
    }
}