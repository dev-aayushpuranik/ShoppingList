package com.aayush.shoppingapp.views

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.UIState
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.common.helper.navigateToScreen
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.databinding.FragmentCategoriesBinding
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.viewModels.CategoriesViewModel
import com.aayush.shoppingapp.views.adapter.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoryViewModel: CategoriesViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mAdapter: CategoryAdapter
    private var mIsReorderingFlagTrue: Boolean = false
    private var mEditableCategoryListModel: CategoryModel? = null
    private var isSaveButtonEnabled = false
    private val Preference_Name = "IsListItemArrangement"
    private val SharedPreferenceDB = "SharedPreferenceDB"
    private var isListArrangement = false
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        categoryViewModel = ViewModelProvider(requireActivity())[CategoriesViewModel::class.java]
        sharedPref = requireContext().getSharedPreferences(SharedPreferenceDB, MODE_PRIVATE)
        setView()
        loadData()
        setAddCategoryView()

        return binding.root
    }

    private fun setView() {
        mAdapter = CategoryAdapter({
            navigateToSubTaskList(it)
        }, {
            navigateToEditPage(it)
        })
        binding.categoriesRV.adapter = mAdapter
        binding.itemArrangeIcon.setOnClickListener {
            isListArrangement = !isListArrangement
            rearrangeView(isListArrangement)

            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putBoolean(Preference_Name, isListArrangement)
            editor.commit()
        }
        binding.progressbar.SetViewVisible(true)
        binding.bottomSheetLayout.isImportantLayout.visibility = View.GONE
        binding.addSubTaskFAB.setOnClickListener {
            setBottomSheetStateExpand()
        }

        binding.settingsMenu.setOnClickListener {
            navigateToSettingsPage()
        }

        // priority spinner
        val arrayAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_level,
            android.R.layout.simple_list_item_1
        )
        binding.bottomSheetLayout.prioritySelector.adapter = arrayAdapter
        binding.bottomSheetLayout.prioritySelector.setSelection(PRIORITY.LOW.value - 1)

        categoryViewModel.uiStateUpdate.observe(viewLifecycleOwner) {
            when (categoryViewModel.uiStateUpdate.value) {
                is UIState.Error -> {
                    UIHelper.showAlertDialog(
                        requireContext(),
                        "Error",
                        "Something went wrong while saving data",
                        {})
                }

                UIState.Loading -> {}
                is UIState.Success -> {
                    Toast.makeText(requireContext(), "Saved Successfully", Toast.LENGTH_SHORT)
                        .show()
                    categoryViewModel.getCategoriesFromDB()
                }

                else -> {}
            }
        }
    }

    private fun navigateToSettingsPage() {
        (activity as MainActivity).navigateToSettingsPage()
    }

    private fun navigateToEditPage(categoryModel: CategoryModel) {
        navigateToScreen(R.id.container,
            EditFragment(categoryModel), "EditFragment")
    }

    private fun rearrangeView(isListArrangement: Boolean) {
        if (isListArrangement) {
            binding.categoriesRV.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.itemArrangeIcon.setImageResource(R.drawable.baseline_grid_view_24)
        } else {
            binding.categoriesRV.layoutManager = GridLayoutManager(context, 2)
            binding.itemArrangeIcon.setImageResource(R.drawable.baseline_list_24)
        }

        mAdapter.notifyDataSetChanged()
    }

    private fun loadData() {
        categoryViewModel.getCategoriesFromDB()
        observeData()
        isListArrangement = sharedPref.getBoolean(Preference_Name, false)
        rearrangeView(isListArrangement)
    }

    private fun observeData() {
        val categoryDataObserver = Observer<List<CategoryModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressbar.SetViewVisible(false)
                binding.noTaskView.SetViewVisible(it.isEmpty())
                mAdapter.data = it
                setRowSwipeForRV()
                if ((categoryViewModel.categories.value?.count() ?: 0) < 2) {
                    rearrangeView(isListArrangement)
                }
            }
        }
        categoryViewModel.categories.observe(this.viewLifecycleOwner, categoryDataObserver)
    }

    private fun setRowSwipeForRV() {
        val sh = SwipeHelper(requireContext()) { viewHolder: RecyclerView.ViewHolder, _: Int ->
            if (categoryViewModel.categories.value?.isNotEmpty() == true
                && (categoryViewModel.categories.value?.get(viewHolder.adapterPosition)?.CategoryId != 0L)
            ) {
                UIHelper.showDeleteAlertDialog(requireContext(), {
                    deleteItemOnOkButtonClick(viewHolder.adapterPosition)
                }, {
                    mAdapter.notifyDataSetChanged()
                })
            } else {
                UIHelper.showAlertDialog(
                    requireContext(),
                    getString(R.string.unable_to_delete),
                    getString(R.string.imp_item_delete_message)
                ) {
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(sh)
        itemTouchHelper.attachToRecyclerView(binding.categoriesRV)
    }

    private fun setAddCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.addCategoryView)
        setBottomSheetStateCollapse()
        enableSaveButton()

        binding.bottomSheetLayout.categoryNameTV.doOnTextChanged { _, _, _, count ->
            isSaveButtonEnabled = count > 0
            enableSaveButton()
        }
        binding.bottomSheetLayout.subTaskNameInputLayout.SetViewVisible(false)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })

        binding.bottomSheetLayout.addCategoryTitle.setOnClickListener {
            setBottomSheetStateCollapseOrExpand()
        }
    }

    private fun enableSaveButton() {
        if (isSaveButtonEnabled) {
            binding.bottomSheetLayout.saveTaskBtn.setTextColor(getColor(R.color.app_text_color))
            binding.bottomSheetLayout.saveTaskBtn.setOnClickListener {
                if (binding.bottomSheetLayout.categoryNameTV.text.toString().trim().isNotEmpty()) {
                    setBottomSheetStateCollapse()
                    binding.bottomSheetLayout.categoryNameTV.clearFocus()
                    binding.bottomSheetLayout.categoryDescriptionTv.clearFocus()

                    if (mIsReorderingFlagTrue) {
                        updateCategoryDataToDB()
                    } else {
                        addCategoryDataToDB()
                    }
                    binding.bottomSheetLayout.categoryNameTV.text = null
                    binding.bottomSheetLayout.categoryDescriptionTv.text = null
                } else {
                    UIHelper.toast(requireContext(), getString(R.string.task_name_cannot_be_empty))
                }
                if ((categoryViewModel.categories.value?.count() ?: 0) < 2) {
                    rearrangeView(isListArrangement)
                }
            }
        } else {
            binding.bottomSheetLayout.saveTaskBtn.setTextColor(getColor(R.color.separator_color))
            binding.bottomSheetLayout.saveTaskBtn.setOnClickListener(null)
        }
    }

    private fun updateCategoryDataToDB() {
        val categoryModel = CategoryModel(
            mEditableCategoryListModel?.CategoryId ?: Date().time,
            binding.bottomSheetLayout.categoryNameTV.text.toString().trim(),
            binding.bottomSheetLayout.categoryDescriptionTv.text.toString().trim(),
            getSelectedPriorityForTask()
        )
        updateCategoryItemToDB(categoryModel)

        mIsReorderingFlagTrue = false
        mEditableCategoryListModel = null
        setBottomSheetStateCollapse()
        binding.bottomSheetLayout.addCategoryTitle.text = getString(R.string.add_new_Item)
    }

    private fun addCategoryDataToDB() {
        val priority = getSelectedPriorityForTask()
        val categoryModel = CategoryModel(
            Date().time,
            binding.bottomSheetLayout.categoryNameTV.text.toString().trim(),
            binding.bottomSheetLayout.categoryDescriptionTv.text.toString().trim(),
            priority
        )
        categoryViewModel.addNewCategory(categoryModel)
    }

    private fun getSelectedPriorityForTask(): PRIORITY {
        val priorityIndex = binding.bottomSheetLayout.prioritySelector.selectedItemPosition
        return if (priorityIndex + 1 == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex + 1 == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
    }

    private fun updateCategoryItemToDB(categoryModel: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.updateCategory(categoryModel)
        }
    }

    private fun deleteCategoryItemFromDB(item: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.deleteCategoryItemFromDB(item)
        }
    }

    private fun setBottomSheetStateCollapseOrExpand() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            setBottomSheetStateExpand()
        } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            setBottomSheetStateCollapse()
        }
    }

    private fun setBottomSheetStateCollapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setBottomSheetStateExpand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun navigateToSubTaskList(category: CategoryModel) {
        setBottomSheetStateCollapse()
        val importantItem = categoryViewModel.subCategories.filter { it.isImportant }
        navigateToScreen(
            R.id.container,
            SubtaskListFragment(category, importantItem),
            "SubtaskListFragmentStack"
        )
    }

    private fun getColor(color: Int) = ContextCompat.getColor(requireContext(), color)

    private fun deleteItemOnOkButtonClick(position: Int) {
        val item = categoryViewModel.categories.value?.get(position)
        item?.let {
            deleteCategoryItemFromDB(it)
        }
    }

    override fun onDestroyView() {
        mIsReorderingFlagTrue = false
        mEditableCategoryListModel = null
        super.onDestroyView()
    }
}