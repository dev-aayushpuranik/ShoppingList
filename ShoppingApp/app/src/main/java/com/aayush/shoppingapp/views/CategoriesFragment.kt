package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.views.adapter.CategoryAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_categories, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.categoriesRV)
        val adapter = CategoryAdapter {
            navigateToSubTaskList()
        }
        adapter.data = getList()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        view.findViewById<FloatingActionButton>(R.id.addCategoryFAB).setOnClickListener {

        }

        return view
    }

    private fun getList() : List<CategoryModel> {
        val arrayList = ArrayList<CategoryModel>()
        for (i in 0 until 10) {
            arrayList.add(CategoryModel("$i","Shopping List $i","General Household items", (10 + i *2),i+2))
        }
        return arrayList.toList()
    }

    private fun navigateToSubTaskList() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, SubtaskListFragment.newInstance(""))
            .addToBackStack("SubtaskListFragmentStack")
            .commit()
    }
}


