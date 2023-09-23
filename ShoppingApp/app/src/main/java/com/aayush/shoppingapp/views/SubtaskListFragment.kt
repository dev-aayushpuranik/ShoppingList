package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.extensions.orDefaut
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.viewModels.SubCategoryViewModel
import com.aayush.shoppingapp.views.adapter.SubCategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [SubtaskListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubtaskListFragment(private val CategoryId: Long) : Fragment() {
    private lateinit var subCategoryViewModel: SubCategoryViewModel

    private lateinit var rootView: View
    private var progressBar: ProgressBar? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetView: ConstraintLayout
    private lateinit var bottomSheetHeader: TextView
    private lateinit var bottomSheetSubCategoryName: TextView
    private lateinit var bottomSheetSubCategoryDescription: TextView
    private lateinit var completedListHeaderView: ConstraintLayout
    private lateinit var arrowIcon: ImageView
    private lateinit var completedHeaderTextView: TextView
    private lateinit var bottomSheetSaveButton: Button
    private lateinit var pendingRecyclerView: RecyclerView
    private lateinit var completedRecyclerView: RecyclerView
    private lateinit var pendingTaskAdapter: SubCategoryAdapter
    private lateinit var completedTaskAdapter: SubCategoryAdapter

    private var mIsHeaderCollapsed:Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_subtask_list, container, false)
        subCategoryViewModel =
            ViewModelProvider(requireActivity())[SubCategoryViewModel::class.java]

        loadData()
        setView()
        setAddSubCategoryView()

        return rootView
    }

    private fun loadData() {
        subCategoryViewModel.mCategoryId = CategoryId
        subCategoryViewModel.getSubCategoriesFromDB(requireContext())
    }

    private fun setView() {
        bottomSheetView = rootView.findViewById(R.id.add_Subcategory_view)
        bottomSheetHeader = rootView.findViewById(R.id.addSubCategoryTitle)
        bottomSheetSubCategoryName = bottomSheetView.findViewById(R.id.subCategoryNameTV)
        bottomSheetSubCategoryDescription =
            bottomSheetView.findViewById(R.id.subCategoryDescriptionTv)
        bottomSheetSaveButton =
            bottomSheetView.findViewById<AppCompatButton>(R.id.saveSubCategoryItemBtn)

        arrowIcon = rootView.findViewById(R.id.arrow_icon)
        completedListHeaderView = rootView.findViewById(R.id.constraintLayout)
        completedHeaderTextView = rootView.findViewById(R.id.completedListHeader)

        pendingRecyclerView = rootView.findViewById(R.id.subtaskRV)
        pendingTaskAdapter = SubCategoryAdapter(requireContext(), {

        }, {
            subCategoryViewModel.updateCategoryItem(requireContext(), it)
        })
        pendingRecyclerView.adapter = pendingTaskAdapter
        pendingRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val sh = SwipeHelper(requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: SubCategoryListModel = pendingTaskAdapter.data[viewHolder.adapterPosition]
                deleteSubCategoryItemFromDB(item)
                Snackbar.make(pendingRecyclerView, "Deleted " + item.subtaskName, Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        addSubCategoryItemToDB(item)
                        pendingTaskAdapter.notifyItemRangeChanged(0,
                            subCategoryViewModel.subCategories.value?.size.orDefaut())
                    }.show()
            })

        val pendingItemTouchHelper = ItemTouchHelper(sh)
        pendingItemTouchHelper.attachToRecyclerView(pendingRecyclerView)

        completedRecyclerView = rootView.findViewById(R.id.completedSubtaskRV)
        completedTaskAdapter = SubCategoryAdapter(requireContext(), {

        }, {
            subCategoryViewModel.updateCategoryItem(requireContext(), it)
        })
        completedRecyclerView.adapter = completedTaskAdapter
        completedRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val completedSh = SwipeHelper(requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, _: Int ->
                val item: SubCategoryListModel = completedTaskAdapter.data[viewHolder.adapterPosition]
                deleteSubCategoryItemFromDB(item)
                Snackbar.make(completedRecyclerView, "Deleted " + item.subtaskName, Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        addSubCategoryItemToDB(item)
                        completedTaskAdapter.notifyItemRangeChanged(0,
                            subCategoryViewModel.subCategories.value?.size.orDefaut())
                    }.show()
            })

        val completedITH = ItemTouchHelper(completedSh)
        completedITH.attachToRecyclerView(completedRecyclerView)

        rootView.findViewById<FloatingActionButton>(R.id.addSubTaskFAB).setOnClickListener {
            setBottomSheetStateExpand()
        }

        progressBar = rootView.findViewById(R.id.progressbar)
        progressBar?.SetViewVisible(true)
        subCategoryViewModel.getSubCategoriesFromDB(requireContext())
        val categoryDataObserver = Observer<List<SubCategoryListModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                progressBar?.SetViewVisible(false)
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
                completedRecyclerView.SetViewVisible(completedItems.count() > 0)
                completedListHeaderView.SetViewVisible(completedItems.count() > 0)
                completedTaskAdapter.data = completedItems
                pendingTaskAdapter.data = pendingItems
            }
        }
        subCategoryViewModel.subCategories.observe(this.viewLifecycleOwner, categoryDataObserver)
    }

    private fun setAddSubCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        setBottomSheetStateCollapse()

        bottomSheetSaveButton.setOnClickListener {
            if (bottomSheetSubCategoryName.text.toString().trim().isNotEmpty()) {
                setBottomSheetStateCollapse()
                bottomSheetSubCategoryName.clearFocus()
                bottomSheetSubCategoryDescription.clearFocus()

                addSubCategoryItemToDB()

                bottomSheetSubCategoryName.text = null
                bottomSheetSubCategoryDescription.text = null
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
            subtaskName = bottomSheetSubCategoryName.text.toString(),
            subtaskDescription = bottomSheetSubCategoryDescription.text.toString(),
            isTaskDone = false,
            isImportant = false
        )
        if (!bottomSheetSubCategoryName.text.trim().isNullOrEmpty()) {
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