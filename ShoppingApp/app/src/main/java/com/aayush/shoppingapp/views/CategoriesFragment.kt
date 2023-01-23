package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.viewModels.CategoriesViewModel
import com.aayush.shoppingapp.views.adapter.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CategoriesFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var categoryViewModel: CategoriesViewModel
    private var progressBar: ProgressBar? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var BottomSheetHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_categories, container, false)

        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoriesViewModel::class.java)

        setView()
        setAddCategoryView()

        return rootView
    }

    private fun setView() {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.categoriesRV)
        val adapter = CategoryAdapter {
            navigateToSubTaskList()
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        rootView.findViewById<FloatingActionButton>(R.id.addCategoryFAB).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        progressBar = rootView.findViewById(R.id.progressbar)
        progressBar?.visibility = View.VISIBLE
        val categoryDataObserver = Observer<List<CategoryModel>> {
            CoroutineScope(Dispatchers.Main).launch {
                progressBar?.visibility = View.GONE
                adapter.data = it
            }
        }
        categoryViewModel.categories.observe(this.viewLifecycleOwner,categoryDataObserver)
    }

    private fun setAddCategoryView() {
        BottomSheetHeader = rootView.findViewById(R.id.addCategoryTitle)
        val bottomSheet = rootView.findViewById<ConstraintLayout>(R.id.add_category_view)

        val saveButton = bottomSheet.findViewById<AppCompatButton>(R.id.saveTaskBtn)
        val titleTV = bottomSheet.findViewById<TextView>(R.id.categoryNameTV)
        val discriptionTV = bottomSheet.findViewById<TextView>(R.id.categoryDescriptionTv)

        saveButton.setOnClickListener {
            if(titleTV.text.toString().trim().length > 0) {
                val categoryModel = CategoryModel(Date().time, titleTV.text.toString().trim(), discriptionTV.text.toString().trim())
                CoroutineScope(Dispatchers.IO).launch {
                    categoryViewModel.addNewCategory(requireContext(), categoryModel)
                }
            } else {
                Toast.makeText(requireContext(), "Tite cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                var title = getString(R.string.add_new_list)
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    title += "(+)"
                    BottomSheetHeader.setText(title)
                } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    title += "(-)"
                    BottomSheetHeader.setText(title)
                }
            }
        })

        BottomSheetHeader.setOnClickListener {
            if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun navigateToSubTaskList() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, SubtaskListFragment.newInstance(""))
            .addToBackStack("SubtaskListFragmentStack")
            .commit()
    }
}


