package com.aayush.shoppingapp.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aayush.shoppingapp.Repository.CategoryRepository
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesViewModel : ViewModel() {

    val categories: MutableLiveData<List<CategoryModel>> by lazy {
        MutableLiveData<List<CategoryModel>>()
    }

    private var mRepo: CategoryRepository? = CategoryRepository.getInstance()

    public suspend fun addNewCategory(context: Context, categoryModel: CategoryModel) {
        mRepo?.addNewCategory(context, categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {})
    }

    public suspend fun deleteCategoryItemFromDB(context: Context, categoryModel: CategoryModel) {
        mRepo?.deleteCategoryItemFromDB(context, categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {
            getCategoriesFromDB(context)
        })
    }

    public fun getCategoriesFromDB(context: Context) {
        mRepo = CategoryRepository.getInstance()

        mRepo?.getCategories(context) {
            CoroutineScope(Dispatchers.Main).launch {
                categories.value = getCategories(it)
            }
        }
    }

    private fun getCategories(tables: List<CategoryTable>?): List<CategoryModel> {
        val arrayList = arrayListOf<CategoryModel>()
        tables?.forEach {
            getCategory(it)?.let { it1 -> arrayList.add(it1) }
        }
        return arrayList.toList()
    }

    private fun getCategory(table: CategoryTable?): CategoryModel? {
        var model: CategoryModel? = null
        table?.let {
            model = CategoryModel(
                it.id,
                it.CategoryTitle,
                it.CategoryDescription
            )
        }
        return model
    }
}