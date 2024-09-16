package com.aayush.shoppingapp.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.databinding.FragmentEditBinding
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.viewModels.SubCategoryViewModel

class EditFragment(private val subCategoryListModel: SubCategoryListModel) : Fragment() {

    private lateinit var binding: FragmentEditBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        val subCategoryViewModel = ViewModelProvider(this).get(SubCategoryViewModel::class.java)

        binding.toolbar.toolbarTitle.text = subCategoryListModel.subtaskName
        binding.nameTxtVw.text = Editable.Factory.getInstance().newEditable(subCategoryListModel.subtaskName)
        binding.descriptionTxtVw.text = Editable.Factory.getInstance().newEditable(subCategoryListModel.subtaskDescription)
        binding.toolbar.saveBtn.SetViewVisible(true)

        setImportantIcon()
        binding.isImportantIv.setOnClickListener {
            subCategoryListModel.isImportant = !subCategoryListModel.isImportant
            setImportantIcon()
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
        binding.prioritySelector.setSelection(subCategoryListModel.priorityId.value-1, false)

        binding.toolbar.saveBtn.setOnClickListener {
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

        return binding.root
    }

    private fun isValidated(): Boolean {
        return (subCategoryListModel.subtaskName == binding.nameTxtVw.text.toString() &&
                subCategoryListModel.subtaskDescription == binding.descriptionTxtVw.text.toString() &&
                subCategoryListModel.priorityId == getSelectedPriorityForTask())
    }

    private fun getSelectedPriorityForTask(): PRIORITY {
        val priorityIndex = binding.prioritySelector.selectedItemPosition
        return if (priorityIndex + 1 == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex + 1 == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
    }

    private fun setImportantIcon() {
        if(subCategoryListModel.isImportant) {
            binding.isImportantIv.setImageDrawable(returnDrawable(R.drawable.important_icon))
        } else {
            binding.isImportantIv.setImageDrawable(returnDrawable(R.drawable.unimportant_icon))
        }
    }

    private fun returnDrawable(icon: Int) : Drawable? {
        return ContextCompat.getDrawable(requireContext(), icon)
    }
}