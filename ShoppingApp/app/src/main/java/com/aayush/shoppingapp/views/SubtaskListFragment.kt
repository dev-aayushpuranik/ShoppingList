package com.aayush.shoppingapp.views

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.databinding.FragmentSubtaskListBinding
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.viewModels.SubCategoryViewModel
import com.aayush.shoppingapp.views.adapter.SubCategoryAdapter
import com.aayush.shoppingapp.views.adapter.SubCategoryCompletedTaskAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date


class SubtaskListFragment() : Fragment(), OnDayNightStateChanged {

    private var categoryModel: CategoryModel? = null
    private var subCategoryViewModel: SubCategoryViewModel? = null
    private lateinit var binding: FragmentSubtaskListBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var pendingTaskAdapter: SubCategoryAdapter
    private lateinit var completedTaskAdapter: SubCategoryCompletedTaskAdapter
    private var mSubcategoryImportantItems = listOf<SubCategoryListModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentSubtaskListBinding.inflate(inflater, container, false)
        subCategoryViewModel =
            ViewModelProvider(requireActivity())[SubCategoryViewModel::class.java]

        loadData()
        setView()
        setAddSubCategoryView()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        return binding.root
    }

    constructor(categoryModel: CategoryModel, importantList: List<SubCategoryListModel>?) : this() {
        importantList?.let {
            this.mSubcategoryImportantItems = it
        }
        this.categoryModel = categoryModel
    }

    private fun loadData() {
        categoryModel?.CategoryId?.let {
            subCategoryViewModel?.mCategoryId = it
            if(it == 0L) {
                subCategoryViewModel?.subCategories?.value = mSubcategoryImportantItems
            } else {
                subCategoryViewModel?.getSubCategoriesFromDB(requireContext())
            }
        }
    }

    private fun setView() {
        (requireActivity() as MainActivity).setToolbar(categoryModel?.CategoryName?: "") {
            navigateToPreviousScreen()
        }
        pendingTaskAdapter = SubCategoryAdapter(requireContext(), {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        }, {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        })
        binding.subtaskRV.adapter = pendingTaskAdapter
        binding.subtaskRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val sh = SwipeHelper(requireContext(), null,
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: SubCategoryListModel = pendingTaskAdapter.data[viewHolder.adapterPosition]
                deleteSubCategoryItemFromDB(item)
                UIHelper.snackBar(binding.subtaskRV, "Deleted " + item.subtaskName, "Undo") {
                    addSubCategoryItemToDB(item)
                    pendingTaskAdapter.notifyDataSetChanged()
                }
            })
        val pendingItemTouchHelper = ItemTouchHelper(sh)
        pendingItemTouchHelper.attachToRecyclerView(binding.subtaskRV)


        completedTaskAdapter = SubCategoryCompletedTaskAdapter(requireContext(), {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        }, {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        })
        binding.completedSubtaskRV.adapter = completedTaskAdapter
        binding.completedSubtaskRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val completedSh = SwipeHelper(requireContext(), null,
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: SubCategoryListModel = completedTaskAdapter.data[viewHolder.adapterPosition]
                deleteSubCategoryItemFromDB(item)

                UIHelper.snackBar(binding.completedSubtaskRV, "Deleted " + item.subtaskName, "Undo") {
                    addSubCategoryItemToDB(item)
                    completedTaskAdapter.notifyDataSetChanged()
                }
            })

        val completedITH = ItemTouchHelper(completedSh)
        completedITH.attachToRecyclerView(binding.completedSubtaskRV)

        binding.addSubTaskFAB.setOnClickListener {
            setBottomSheetStateExpand()
        }

        binding.progressbar.SetViewVisible(true)
        subCategoryViewModel?.getSubCategoriesFromDB(requireContext())
        val categoryDataObserver = Observer<List<SubCategoryListModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressbar.SetViewVisible(false)
                if(pendingTaskAdapter.data.isEmpty() && completedTaskAdapter.data.isEmpty()) { setBottomSheetStateExpand() } else setBottomSheetStateCollapse()
                val completedItems = arrayListOf<SubCategoryListModel>()
                val pendingItems = arrayListOf<SubCategoryListModel>()
                for(i in 0 until it.count()) {
                    val item = it[i]
                    if(item.isTaskDone) {
                        completedItems.add(item)
                    } else {
                        pendingItems.add(item)
                    }
                }
                binding.completedSubtaskRV.SetViewVisible(completedItems.isNotEmpty())
                binding.constraintLayout.SetViewVisible(completedItems.isNotEmpty())
                completedTaskAdapter.data = completedItems
                pendingTaskAdapter.data = pendingItems
            }
        }
        subCategoryViewModel?.subCategories?.observe(this.viewLifecycleOwner, categoryDataObserver)

        binding.bottomSheetLayout.addCategoryTitle.setOnClickListener {
            setBottomSheetStateCollapseOrExpand()
        }

        binding.constraintLayout.setOnClickListener {
            if(binding.completedSubtaskRV.height != 0) {subCategoryViewModel?.completedTaskHeight = binding.completedSubtaskRV.height}
            if(binding.completedSubtaskRV.height != 0) {
                binding.arrowIcon.animate().rotationBy(-90f)
                    .duration = 500
                UIHelper.collapse(binding.completedSubtaskRV, 500, 0)

            } else {
                binding.arrowIcon.animate().rotationBy(90f)
                    .duration = 100
                UIHelper.expand(binding.completedSubtaskRV, 500, subCategoryViewModel?.completedTaskHeight ?: 200)
            }
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

    private fun setAddSubCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.addCategoryView)
        setBottomSheetStateCollapse()

        binding.bottomSheetLayout.saveTaskBtn.setOnClickListener {
            if (binding.bottomSheetLayout.categoryNameTV.text.toString().trim().isNotEmpty()) {
                setBottomSheetStateCollapse()
                binding.bottomSheetLayout.categoryNameTV.clearFocus()
                binding.bottomSheetLayout.categoryDescriptionTv.clearFocus()

                addSubCategoryItemToDB()

                binding.bottomSheetLayout.categoryNameTV.text = null
                binding.bottomSheetLayout.categoryDescriptionTv.text = null
            } else {
                UIHelper.toast(requireContext(), getString(R.string.task_name_cannot_be_empty))
            }
        }
    }

    private fun addSubCategoryItemToDB() {
        categoryModel?.let { categoryModel ->
            val item = SubCategoryListModel(
                subtaskItemId = Date().time,
                categoryId = categoryModel.CategoryId,
                subtaskName = binding.bottomSheetLayout.categoryNameTV.text.toString(),
                subtaskDescription = binding.bottomSheetLayout.categoryDescriptionTv.text.toString(),
                isTaskDone = false,
                isImportant = false
            )
            if (!binding.bottomSheetLayout.categoryNameTV.text?.trim().isNullOrEmpty()) {
                addSubCategoryItemToDB(item)
            } else {
                subCategoryViewModel?.errorModel?.value =
                    getString(R.string.task_name_cannot_be_empty)
            }
        }
    }

    private fun navigateToPreviousScreen() {
        try {
            requireFragmentManager().popBackStack()
            (requireActivity() as MainActivity).setToolbar(getString(R.string.app_name), null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun addSubCategoryItemToDB(subCategoryListModel: SubCategoryListModel) {
        subCategoryViewModel?.addNewSubCategoryItem(requireContext(), subCategoryListModel)
    }

    private fun deleteSubCategoryItemFromDB(item: SubCategoryListModel) {
        subCategoryViewModel?.deleteSubCategoryItemFromDB(requireContext(), item)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            try {
                navigateToPreviousScreen()
            } catch (_: Exception) {}
        }
    }

    override fun onDayNightApplied(state: Int) {
        applyDayNightMode()
        completedTaskAdapter.notifyDataSetChanged()
        pendingTaskAdapter.notifyDataSetChanged()
    }

    private fun applyDayNightMode() {
        binding.bottomSheetLayout.addCategoryTitle.background = ContextCompat.getDrawable(requireContext(), R.color.headerColor)
        binding.bottomSheetLayout.addCategoryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
        binding.bottomSheetLayout.addCategoryView.background = ContextCompat.getDrawable(requireContext(), R.color.recycler_row_view_bg)
        binding.bottomSheetLayout.categoryNameTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
        binding.bottomSheetLayout.categoryDescriptionTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
        binding.bottomSheetLayout.categoryNameTV.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.toolbarColor)))
        binding.bottomSheetLayout.subTaskNameInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.toolbarColor))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ((requireActivity() as MainActivity).registerBackPressEvent())
    }
}