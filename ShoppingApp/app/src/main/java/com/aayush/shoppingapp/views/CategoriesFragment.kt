package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.orDefaut
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

class CategoriesFragment : Fragment() {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoryViewModel: CategoriesViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        categoryViewModel =
            ViewModelProvider(requireActivity())[CategoriesViewModel::class.java]

        setView()
        setAddCategoryView()

        return binding.root
    }

    private fun setView() {
        mAdapter = CategoryAdapter { navigateToSubTaskList(it.CategoryId) }
        binding.categoriesRV.adapter = mAdapter
        binding.categoriesRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val sh = SwipeHelper(
            requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: CategoryModel = mAdapter.data[viewHolder.adapterPosition]
                deleteCategoryItemFromDB(item)

                UIHelper.snackBar(binding.categoriesRV, "Deleted " + item.CategoryName, "Undo") {
                    addCategoryItemToDB(item)
                    mAdapter.notifyItemRangeChanged(0, categoryViewModel.categories.value?.size.orDefaut())
                }
            })

        val itemTouchHelper = ItemTouchHelper(sh)
        itemTouchHelper.attachToRecyclerView(binding.categoriesRV)

        binding.addCategoryFAB.setOnClickListener {
            setBottomSheetStateExpand()
        }

        binding.progressbar.visibility = View.VISIBLE
        categoryViewModel.getCategoriesFromDB(requireContext())
        val categoryDataObserver = Observer<List<CategoryModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressbar.visibility = View.GONE
                mAdapter.data = it
            }
        }
        categoryViewModel.categories.observe(this.viewLifecycleOwner, categoryDataObserver)
    }

    private fun setAddCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.addCategoryView)
        setBottomSheetStateCollapse()

        binding.bottomSheetLayout.saveTaskBtn.setOnClickListener {
            if (binding.bottomSheetLayout.categoryNameTV.text.toString().trim().isNotEmpty()) {
                setBottomSheetStateCollapse()
                binding.bottomSheetLayout.categoryNameTV.clearFocus()
                binding.bottomSheetLayout.categoryDescriptionTv.clearFocus()

                addCategoryDataToDB()

                binding.bottomSheetLayout.categoryNameTV.text = null
                binding.bottomSheetLayout.categoryDescriptionTv.text = null
            } else {
                UIHelper.toast(requireContext(), "Category Name field cannot be empty")
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                var title = getString(R.string.add_new_list)
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    title += ""
                    binding.bottomSheetLayout.addCategoryTitle.text = title
                } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    title += "(+)"
                    binding.bottomSheetLayout.addCategoryTitle.text = title
                }
            }
        })

        binding.bottomSheetLayout.addCategoryTitle.setOnClickListener {
            setBottomSheetStateCollapseOrExpand()
        }
    }

    private fun addCategoryDataToDB() {
        val categoryModel = CategoryModel(
            Date().time,
            binding.bottomSheetLayout.categoryNameTV.text.toString().trim(),
            binding.bottomSheetLayout.categoryDescriptionTv.text.toString().trim()
        )
        addCategoryItemToDB(categoryModel)
    }

    private fun addCategoryItemToDB(categoryModel: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.addNewCategory(requireContext(), categoryModel)
        }
    }

    private fun updateCategoryList(categories: List<CategoryModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryViewModel.updateCategoryList(requireContext(), categories)
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

    private fun navigateToSubTaskList(categoryId: Long) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, SubtaskListFragment(categoryId))
            .addToBackStack("SubtaskListFragmentStack")
            .commit()
    }
}


