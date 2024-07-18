package com.aayush.shoppingapp.views

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.OnDayNightStateChanged
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.Enums.PRIORITY
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
    private var mIsReorderingFlagTrue: Boolean = false
    private var mEditableCategoryListModel: CategoryModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        categoryViewModel = ViewModelProvider(requireActivity())[CategoriesViewModel::class.java]
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
        binding.categoriesRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        addReorderLogicToRecyclerView()
        binding.progressbar.SetViewVisible(true)
        binding.bottomSheetLayout.isImportantCheckbox.visibility = View.GONE
        binding.addSubTaskFAB.setOnClickListener {
            setBottomSheetStateExpand()
        }

        // priority spinner
        val arrayAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_level,
            android.R.layout.simple_list_item_1
        )
        binding.bottomSheetLayout.prioritySelector.adapter = arrayAdapter
        binding.bottomSheetLayout.prioritySelector.setSelection(PRIORITY.LOW.value - 1)
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
        val sh = SwipeHelper(requireContext()) { viewHolder: RecyclerView.ViewHolder, _: Int ->
            if (categoryViewModel.categories.value?.isNotEmpty() == true
                && (categoryViewModel.categories.value?.get(viewHolder.adapterPosition)?.CategoryId != 0L)) {
                UIHelper.showDeleteAlertDialog(requireContext(), {
                    deleteItemOnOkButtonClick(viewHolder.adapterPosition)
                }, {
                    mAdapter.notifyDataSetChanged()
                })
            } else {
                UIHelper.showAlertDialog(
                    requireContext(), "Unable to Delete",
                    "This is not a deletable item because it just showes the list of important items and it is added only if there are any important items marked"
                ) {
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(sh)
        itemTouchHelper.attachToRecyclerView(binding.categoriesRV)
    }

    private fun addReorderLogicToRecyclerView() {
        val itemTouchHelper by lazy {
            // 1. Note that I am specifying all 4 directions.
            //    Specifying START and END also allows
            //    more organic dragging than just specifying UP and DOWN.
            val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
                    // 1. This callback is called when a ViewHolder is selected.
                    //    We highlight the ViewHolder here.
                    override fun onSelectedChanged(
                        viewHolder: RecyclerView.ViewHolder?,
                        actionState: Int
                    ) {
                        super.onSelectedChanged(viewHolder, actionState)

                        if (actionState == ACTION_STATE_DRAG) {
                            viewHolder?.itemView?.alpha = 0.5f
                        }
                    }

                    // 2. This callback is called when the ViewHolder is
                    //    unselected (dropped). We unhighlight the ViewHolder here.
                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        viewHolder.itemView.alpha = 1.0f
                    }

                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {

                        val adapter = recyclerView.adapter as CategoryAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        // 2. Update the backing model. Custom implementation in
                        //    MainRecyclerViewAdapter. You need to implement
                        //    reordering of the backing model inside the method.
                        val fromItem = categoryViewModel.categories.value?.get(from)
                        val toItem = categoryViewModel.categories.value?.get(to)
                        if (fromItem?.CategoryId != 0L && toItem?.CategoryId != 0L) {
                            adapter.moveItem(from, to)
                            // 3. Tell adapter to render the model update.
                            adapter.notifyItemMoved(from, to)

                            return true
                        } else return false
                    }

                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int
                    ) {
                        // 4. Code block for horizontal swipe.
                        //    ItemTouchHelper handles horizontal swipe as well, but
                        //    it is not relevant with reordering. Ignoring here.
                    }
                }
            ItemTouchHelper(simpleItemTouchCallback)
        }

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
            mEditableCategoryListModel?.priorityId ?: PRIORITY.LOW
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
        addCategoryItemToDB(categoryModel)
    }

    private fun getSelectedPriorityForTask(): PRIORITY {
        val priorityIndex = binding.bottomSheetLayout.prioritySelector.selectedItemPosition
        return if (priorityIndex + 1 == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex + 1 == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
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
        binding.bottomSheetLayout.addCategoryTitle.background =
            ContextCompat.getDrawable(requireContext(), R.color.headerColor)
        binding.bottomSheetLayout.addCategoryTitle.setTextColor(getColor(R.color.app_text_color))
        binding.bottomSheetLayout.addCategoryView.setBackgroundColor(getColor(R.color.recycler_row_view_bg))

        binding.bottomSheetLayout.subTaskNameInputLayout.hintTextColor = ColorStateList.valueOf(getColor(R.color.app_text_color))

        binding.bottomSheetLayout.categoryNameTV.setTextColor(getColor(R.color.app_text_color))
        binding.bottomSheetLayout.categoryDescriptionTv.setHintTextColor(
            ColorStateList.valueOf(getColor(R.color.app_text_color))
        )

        binding.root.requestLayout()
        binding.root.invalidate()
    }

    private fun getColor(color:Int) = ContextCompat.getColor(requireContext(), color)


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