package com.aayush.shoppingapp.views

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.OnDayNightStateChanged
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.databinding.FragmentCategoriesBinding
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.viewModels.CategoriesViewModel
import com.aayush.shoppingapp.views.adapter.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class CategoriesFragment : Fragment(), OnDayNightStateChanged {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoryViewModel: CategoriesViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mAdapter: CategoryAdapter
    private var mIsReorderingFlagTrue:Boolean = false
    private var mEditableCategoryListModel: CategoryModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        categoryViewModel =
            ViewModelProvider(requireActivity())[CategoriesViewModel::class.java]

        setView()
        loadData()
        setAddCategoryView()

        return binding.root
    }

    private fun setView() {
        mAdapter = CategoryAdapter({ navigateToSubTaskList(it) },
            {
                mEditableCategoryListModel = it
                mIsReorderingFlagTrue = true
                setBottomSheetStateExpand()
                binding.bottomSheetLayout.addCategoryTitle.text =
                    "Edit Item ${mEditableCategoryListModel?.CategoryName.orDefault()}"
                binding.bottomSheetLayout.categoryNameTV.text =
                    Editable.Factory.getInstance().newEditable(it.CategoryName)
                binding.bottomSheetLayout.categoryDescriptionTv.text =
                    Editable.Factory.getInstance().newEditable(it.Description)


            })
        binding.categoriesRV.adapter = mAdapter
        binding.categoriesRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.progressbar.SetViewVisible(true)
        binding.bottomSheetLayout.isImportantCheckbox.visibility = View.GONE
        binding.addSubTaskFAB.setOnClickListener {
            setBottomSheetStateExpand()
        }
    }

    private fun loadData() {
        categoryViewModel.getCategoriesFromDB(requireContext())
        observeData()
    }

    private fun observeData() {
        val categoryDataObserver = Observer<List<CategoryModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressbar.SetViewVisible(false)
                binding.noTaskView.SetViewVisible(it.isEmpty())
                mAdapter.data = it
                setRowSwipeForRV()
            }
        }
        categoryViewModel.categories.observe(this.viewLifecycleOwner, categoryDataObserver)
    }

    private fun setRowSwipeForRV() {
        val sh = SwipeHelper(requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                showAlertDialog(viewHolder.adapterPosition)
            })

        val itemTouchHelper = ItemTouchHelper(sh)
        itemTouchHelper.attachToRecyclerView(binding.categoriesRV)
    }

    private fun setAddCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.addCategoryView)
        setBottomSheetStateCollapse()

        binding.bottomSheetLayout.saveTaskBtn.setOnClickListener {
            if (binding.bottomSheetLayout.categoryNameTV.text.toString().trim().isNotEmpty()) {
                setBottomSheetStateCollapse()
                binding.bottomSheetLayout.categoryNameTV.clearFocus()
                binding.bottomSheetLayout.categoryDescriptionTv.clearFocus()

                if(mIsReorderingFlagTrue) {
                    updateCategoryDataToDB()
                } else {
                    addCategoryDataToDB()
                }
                binding.bottomSheetLayout.categoryNameTV.text = null
                binding.bottomSheetLayout.categoryDescriptionTv.text = null
            } else {
                UIHelper.toast(requireContext(), getString(R.string.task_name_cannot_be_empty))
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })

        binding.bottomSheetLayout.addCategoryTitle.setOnClickListener {
            setBottomSheetStateCollapseOrExpand()
        }
    }

    private fun updateCategoryDataToDB() {
        val categoryModel = CategoryModel(
             mEditableCategoryListModel?.CategoryId ?: Date().time,
            binding.bottomSheetLayout.categoryNameTV.text.toString().trim(),
            binding.bottomSheetLayout.categoryDescriptionTv.text.toString().trim(),
            mEditableCategoryListModel?.priorityId ?: categoryViewModel.categories.value?.size.orDefault()
        )
        updateCategoryItemToDB(categoryModel)

        mIsReorderingFlagTrue = false
        mEditableCategoryListModel = null
        setBottomSheetStateCollapse()
        binding.bottomSheetLayout.addCategoryTitle.text = getString(R.string.add_new_Item)
    }

    private fun addCategoryDataToDB() {
        val categoryModel = CategoryModel(
            Date().time,
            binding.bottomSheetLayout.categoryNameTV.text.toString().trim(),
            binding.bottomSheetLayout.categoryDescriptionTv.text.toString().trim(),
            categoryViewModel.categories.value?.size.orDefault()
        )
        addCategoryItemToDB(categoryModel)
    }

    private fun updateCategoryItemToDB(categoryModel: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.updateCategory(requireContext(), categoryModel)
        }
    }

    private fun addCategoryItemToDB(categoryModel: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.addNewCategory(requireContext(), categoryModel)
        }
    }

    private fun deleteCategoryItemFromDB(item: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.deleteCategoryItemFromDB(requireContext(), item)
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
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, SubtaskListFragment(category, importantItem))
            .addToBackStack("SubtaskListFragmentStack")
            .commit()
    }

    override fun onDayNightApplied(state: Int) {
        applyDayNightMode()
        mAdapter.notifyDataSetChanged()
    }

    private fun applyDayNightMode() {
        binding.bottomSheetLayout.addCategoryTitle.background = ContextCompat.getDrawable(requireContext(), R.color.headerColor)
        binding.bottomSheetLayout.addCategoryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
        binding.bottomSheetLayout.addCategoryView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.recycler_row_view_bg))

        binding.bottomSheetLayout.subTaskNameInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.app_text_color))

        binding.bottomSheetLayout.categoryNameTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
        binding.bottomSheetLayout.categoryDescriptionTv.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.app_text_color)))

        binding.root.requestLayout()
        binding.root.invalidate()
    }

    private fun showAlertDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_category_Item_title))
            .setMessage(getString(R.string.delete_category_Item_content))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteItemOnOkButtonClick(position)
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                mAdapter.notifyDataSetChanged()
            }
            .show()
    }

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