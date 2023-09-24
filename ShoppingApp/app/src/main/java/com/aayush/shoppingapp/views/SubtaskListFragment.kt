package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefaut
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.databinding.FragmentSubtaskListBinding
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.viewModels.SubCategoryViewModel
import com.aayush.shoppingapp.views.adapter.SubCategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SubtaskListFragment(private val CategoryId: Long) : Fragment() {
    private lateinit var subCategoryViewModel: SubCategoryViewModel

    private lateinit var binding: FragmentSubtaskListBinding

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var pendingTaskAdapter: SubCategoryAdapter
    private lateinit var completedTaskAdapter: SubCategoryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentSubtaskListBinding.inflate(inflater, container, false)
        subCategoryViewModel =
            ViewModelProvider(requireActivity())[SubCategoryViewModel::class.java]

        loadData()
        setView()
        setAddSubCategoryView()

        return binding.root
    }

    private fun loadData() {
        subCategoryViewModel.mCategoryId = CategoryId
        subCategoryViewModel.getSubCategoriesFromDB(requireContext())
    }

    private fun setView() {
        pendingTaskAdapter = SubCategoryAdapter(requireContext(), {

        }, {
            subCategoryViewModel.updateCategoryItem(requireContext(), it)
        })
        binding.subtaskRV.adapter = pendingTaskAdapter
        binding.subtaskRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val sh = SwipeHelper(requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: SubCategoryListModel = pendingTaskAdapter.data[viewHolder.adapterPosition]
                deleteSubCategoryItemFromDB(item)
                Snackbar.make(binding.subtaskRV, "Deleted " + item.subtaskName, Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        addSubCategoryItemToDB(item)
                        pendingTaskAdapter.notifyItemRangeChanged(0,
                            subCategoryViewModel.subCategories.value?.size.orDefaut())
                    }.show()
            })

        val pendingItemTouchHelper = ItemTouchHelper(sh)
        pendingItemTouchHelper.attachToRecyclerView(binding.subtaskRV)


        completedTaskAdapter = SubCategoryAdapter(requireContext(), {

        }, {
            subCategoryViewModel.updateCategoryItem(requireContext(), it)
        })
        binding.completedSubtaskRV.adapter = completedTaskAdapter
        binding.completedSubtaskRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val completedSh = SwipeHelper(requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: SubCategoryListModel = completedTaskAdapter.data[viewHolder.adapterPosition]
                deleteSubCategoryItemFromDB(item)

                UIHelper.snackBar(binding.completedSubtaskRV, "Deleted " + item.subtaskName, "Undo") {
                    addSubCategoryItemToDB(item)
                    completedTaskAdapter.notifyItemRangeChanged(0,
                        subCategoryViewModel.subCategories.value?.size.orDefaut())
                }
            })

        val completedITH = ItemTouchHelper(completedSh)
        completedITH.attachToRecyclerView(binding.completedSubtaskRV)

        binding.addSubTaskFAB.setOnClickListener {
            setBottomSheetStateExpand()
        }

        binding.progressbar.SetViewVisible(true)
        subCategoryViewModel.getSubCategoriesFromDB(requireContext())
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
        subCategoryViewModel.subCategories.observe(this.viewLifecycleOwner, categoryDataObserver)
    }

    private fun setAddSubCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.addSubcategoryView)
        setBottomSheetStateCollapse()

        binding.bottomSheetLayout.saveSubCategoryItemBtn.setOnClickListener {
            if (binding.bottomSheetLayout.subCategoryNameTV.text.toString().trim().isNotEmpty()) {
                setBottomSheetStateCollapse()
                binding.bottomSheetLayout.subCategoryNameTV.clearFocus()
                binding.bottomSheetLayout.subCategoryDescriptionTv.clearFocus()

                addSubCategoryItemToDB()

                binding.bottomSheetLayout.subCategoryNameTV.text = null
                binding.bottomSheetLayout.subCategoryDescriptionTv.text = null
            } else {
                Toast.makeText(
                    requireContext(),
                    "Category Name field cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setBottomSheetStateCollapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setBottomSheetStateExpand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun addSubCategoryItemToDB() {
        val item = SubCategoryListModel(
            subtaskItemId = Date().time,
            categoryId = CategoryId,
            subtaskName = binding.bottomSheetLayout.subCategoryNameTV.text.toString(),
            subtaskDescription = binding.bottomSheetLayout.subCategoryDescriptionTv.text.toString(),
            isTaskDone = false,
            isImportant = false
        )
        if (!binding.bottomSheetLayout.subCategoryNameTV.text.trim().isNullOrEmpty()) {
            addSubCategoryItemToDB(item)
        } else {
            subCategoryViewModel.errorModel.value = "Task Name cannot be null"
        }
    }

    private fun addSubCategoryItemToDB(subCategoryListModel: SubCategoryListModel) {
        subCategoryViewModel.addNewSubCategoryItem(requireContext(), subCategoryListModel)
    }

    private fun deleteSubCategoryItemFromDB(item: SubCategoryListModel) {
        subCategoryViewModel.deleteSubCategoryItemFromDB(requireContext(), item)
    }
}