package com.aayush.shoppingapp.views

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
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.databinding.FragmentSubtaskListBinding
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.viewModels.SubCategoryViewModel
import com.aayush.shoppingapp.views.adapter.CategoryAdapter
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
            if (categoryModel?.CategoryId != 0L) {
                it.isImportant = !it.isImportant
                subCategoryViewModel?.updateCategoryItem(requireContext(), it)
            }
        }, {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        }, {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        })
        binding.subtaskRV.adapter = pendingTaskAdapter
        binding.subtaskRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val sh = SwipeHelper(requireContext(),
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
            if (categoryModel?.CategoryId != 0L) {
                it.isImportant = !it.isImportant
                subCategoryViewModel?.updateCategoryItem(requireContext(), it)
            }
        }, {
            subCategoryViewModel?.updateCategoryItem(requireContext(), it)
        })
        binding.completedSubtaskRV.adapter = completedTaskAdapter
        binding.completedSubtaskRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        addReorderLogicToRecyclerView()
        addReorderCompletedTaskListToRecyclerView()

        val completedSh = SwipeHelper(requireContext(),
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
                val completedItems = arrayListOf<SubCategoryListModel>()
                val pendingItems = arrayListOf<SubCategoryListModel>()
                binding.noTaskView.SetViewVisible(it.isEmpty())
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
                if(pendingTaskAdapter.data.isEmpty() && completedTaskAdapter.data.isEmpty()) { setBottomSheetStateExpand() } else setBottomSheetStateCollapse()
            }
        }
        subCategoryViewModel?.subCategories?.observe(this.viewLifecycleOwner, categoryDataObserver)
        binding.bottomSheetLayout.root.SetViewVisible(isBottomSheetVisible())
        binding.addSubTaskFAB.SetViewVisible(isBottomSheetVisible())
        if(isBottomSheetVisible()) {
            binding.bottomSheetLayout.isImportantCheckbox.visibility = View.VISIBLE
            binding.bottomSheetLayout.addCategoryTitle.setOnClickListener {
                setBottomSheetStateCollapseOrExpand()
            }
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
    private fun addReorderLogicToRecyclerView() {
        val itemTouchHelper by lazy {
            // 1. Note that I am specifying all 4 directions.
            //    Specifying START and END also allows
            //    more organic dragging than just specifying UP and DOWN.
            val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or
                            ItemTouchHelper.DOWN, 0) {

                    // 1. This callback is called when a ViewHolder is selected.
                    //    We highlight the ViewHolder here.
                    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?,
                                                   actionState: Int) {
                        super.onSelectedChanged(viewHolder, actionState)

                        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                            viewHolder?.itemView?.alpha = 0.5f
                        }
                    }
                    // 2. This callback is called when the ViewHolder is
                    //    unselected (dropped). We unhighlight the ViewHolder here.
                    override fun clearView(recyclerView: RecyclerView,
                                           viewHolder: RecyclerView.ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        viewHolder?.itemView?.alpha = 1.0f
                    }

                    override fun onMove(recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder,
                                        target: RecyclerView.ViewHolder): Boolean {

                        val adapter = recyclerView.adapter as SubCategoryAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        // 2. Update the backing model. Custom implementation in
                        //    MainRecyclerViewAdapter. You need to implement
                        //    reordering of the backing model inside the method.
                        val fromItem = subCategoryViewModel?.subCategories?.value?.get(from)
                        val toItem = subCategoryViewModel?.subCategories?.value?.get(to)
                        if(fromItem?.isImportant != true && toItem?.isImportant != true) {
                            adapter.moveItem(from, to)
                            // 3. Tell adapter to render the model update.
                            adapter.notifyItemMoved(from, to)

                            return true
                        } else if(fromItem?.isImportant == true && toItem?.isImportant == true) {
                            adapter.moveItem(from, to)
                            // 3. Tell adapter to render the model update.
                            adapter.notifyItemMoved(from, to)

                            return true
                        } else return false
                    }
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                          direction: Int) {
                        // 4. Code block for horizontal swipe.
                        //    ItemTouchHelper handles horizontal swipe as well, but
                        //    it is not relevant with reordering. Ignoring here.
                    }
                }
            ItemTouchHelper(simpleItemTouchCallback)
        }

        itemTouchHelper.attachToRecyclerView(binding.subtaskRV)
    }
    private fun addReorderCompletedTaskListToRecyclerView() {
        val itemTouchHelper by lazy {
            // 1. Note that I am specifying all 4 directions.
            //    Specifying START and END also allows
            //    more organic dragging than just specifying UP and DOWN.
            val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or
                            ItemTouchHelper.DOWN, 0) {

                    // 1. This callback is called when a ViewHolder is selected.
                    //    We highlight the ViewHolder here.
                    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?,
                                                   actionState: Int) {
                        super.onSelectedChanged(viewHolder, actionState)

                        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                            viewHolder?.itemView?.alpha = 0.5f
                        }
                    }
                    // 2. This callback is called when the ViewHolder is
                    //    unselected (dropped). We unhighlight the ViewHolder here.
                    override fun clearView(recyclerView: RecyclerView,
                                           viewHolder: RecyclerView.ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        viewHolder?.itemView?.alpha = 1.0f
                    }

                    override fun onMove(recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder,
                                        target: RecyclerView.ViewHolder): Boolean {

                        val adapter = recyclerView.adapter as SubCategoryCompletedTaskAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        // 2. Update the backing model. Custom implementation in
                        //    MainRecyclerViewAdapter. You need to implement
                        //    reordering of the backing model inside the method.
                        val fromItem = subCategoryViewModel?.subCategories?.value?.get(from)
                        val toItem = subCategoryViewModel?.subCategories?.value?.get(to)
                        if(fromItem?.isImportant == true && toItem?.isImportant == true) {
                            adapter.moveItem(from, to)
                            // 3. Tell adapter to render the model update.
                            adapter.notifyItemMoved(from, to)

                            return true
                        } else return false
                    }
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                          direction: Int) {
                        // 4. Code block for horizontal swipe.
                        //    ItemTouchHelper handles horizontal swipe as well, but
                        //    it is not relevant with reordering. Ignoring here.
                    }
                }
            ItemTouchHelper(simpleItemTouchCallback)
        }

        itemTouchHelper.attachToRecyclerView(binding.completedSubtaskRV)
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
                binding.bottomSheetLayout.isImportantCheckbox.visibility = View.VISIBLE
                binding.bottomSheetLayout.isImportantCheckbox.isChecked = false
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
                isImportant = binding.bottomSheetLayout.isImportantCheckbox.isChecked,
                priorityId = subCategoryViewModel?.subCategories?.value?.size.orDefault()
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
            parentFragmentManager.popBackStack()
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
        if(isBottomSheetVisible()) {
            binding.bottomSheetLayout.addCategoryTitle.background = ContextCompat.getDrawable(requireContext(), R.color.headerColor)
            binding.bottomSheetLayout.addCategoryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
            binding.bottomSheetLayout.addCategoryView.background = ContextCompat.getDrawable(requireContext(), R.color.recycler_row_view_bg)

            binding.bottomSheetLayout.categoryNameTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
            binding.bottomSheetLayout.categoryDescriptionTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))

            binding.bottomSheetLayout.isImportantCheckbox.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))
            binding.completedListHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_text_color))

            binding.root.invalidate()
            binding.root.requestLayout()
        }
    }

    private fun isBottomSheetVisible():Boolean = (categoryModel?.CategoryId != 0L)

    override fun onDestroyView() {
        super.onDestroyView()
        ((requireActivity() as MainActivity).registerBackPressEvent())
    }
}
