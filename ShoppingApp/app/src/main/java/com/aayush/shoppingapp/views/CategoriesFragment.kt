package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.helpers.SwipeHelper
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.viewModels.CategoriesViewModel
import com.aayush.shoppingapp.views.adapter.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class CategoriesFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var categoryViewModel: CategoriesViewModel
    private var progressBar: ProgressBar? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var BottomSheetView: ConstraintLayout
    private lateinit var BottomSheetHeader: TextView
    private lateinit var BottomSheetCategoryName: TextView
    private lateinit var BottomSheetDiscription: TextView
    private lateinit var BottomSheetSaveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_categories, container, false)

        categoryViewModel =
            ViewModelProvider(requireActivity()).get(CategoriesViewModel::class.java)

        setView()
        setAddCategoryView()

        return rootView
    }

    private fun setView() {
        BottomSheetView = rootView.findViewById<ConstraintLayout>(R.id.add_category_view)
        BottomSheetHeader = rootView.findViewById(R.id.addCategoryTitle)
        BottomSheetCategoryName = BottomSheetView.findViewById(R.id.categoryNameTV)
        BottomSheetDiscription = BottomSheetView.findViewById(R.id.categoryDescriptionTv)
        BottomSheetSaveButton = BottomSheetView.findViewById<AppCompatButton>(R.id.saveTaskBtn)


        recyclerView = rootView.findViewById(R.id.categoriesRV)
        mAdapter = CategoryAdapter {
            navigateToSubTaskList()
        }
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val sh = SwipeHelper(
            requireContext(),
            onDeleteSwipe = { viewHolder: RecyclerView.ViewHolder, i: Int ->
                val item: CategoryModel = mAdapter.data.get(viewHolder.adapterPosition)
                deleteCategoryItemFromDB(item)
                Snackbar.make(recyclerView, "Deleted " + item.CategoryName, Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        addCategoryItemToDB(item)
                        mAdapter.notifyDataSetChanged()
                    }.show()
            })

        val itemTouchHelper = ItemTouchHelper(sh)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        rootView.findViewById<FloatingActionButton>(R.id.addCategoryFAB).setOnClickListener {
            setBottomSheetStateExpand()
        }

        progressBar = rootView.findViewById(R.id.progressbar)
        progressBar?.visibility = View.VISIBLE
        categoryViewModel.getCategoriesFromDB(requireContext())
        val categoryDataObserver = Observer<List<CategoryModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                progressBar?.visibility = View.GONE
                mAdapter.data = it
            }
        }
        categoryViewModel.categories.observe(this.viewLifecycleOwner, categoryDataObserver)
    }

    private fun setAddCategoryView() {
        bottomSheetBehavior = BottomSheetBehavior.from(BottomSheetView)
        setBottomSheetStateCollapse()

        BottomSheetSaveButton.setOnClickListener {
            if (BottomSheetCategoryName.text.toString().trim().length > 0) {
                setBottomSheetStateCollapse()
                BottomSheetCategoryName.clearFocus()
                BottomSheetDiscription.clearFocus()

                addCategoryDataToDB()

                BottomSheetCategoryName.text = null
                BottomSheetDiscription.text = null
            } else {
                Toast.makeText(requireContext(), "Tite cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                var title = getString(R.string.add_new_list)
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    title += "(+)"
                    BottomSheetHeader.setText(title)
                } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    title += "(-)"
                    BottomSheetHeader.setText(title)
                }
            }
        })

        BottomSheetHeader.setOnClickListener {
            setBottomSheetStateCollapseOrExpand()
        }
    }

    private fun addCategoryDataToDB() {
        val categoryModel = CategoryModel(
            Date().time,
            BottomSheetCategoryName.text.toString().trim(),
            BottomSheetDiscription.text.toString().trim()
        )
        addCategoryItemToDB(categoryModel)
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

    private fun navigateToSubTaskList() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, SubtaskListFragment.newInstance(""))
            .addToBackStack("SubtaskListFragmentStack")
            .commit()
    }
}


