package com.aayush.shoppingapp.viewModels

import android.content.Context
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.Repository.CategoryRepository
import com.aayush.shoppingapp.Repository.SubCategoryRepository
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.models.SubCategoryListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesViewModel : ViewModel() {

    val categories: MutableLiveData<List<CategoryModel>> by lazy {
        MutableLiveData<List<CategoryModel>>()
    }

    val subCategories: ArrayList<SubCategoryListModel> by lazy {
        arrayListOf()
    }

    private var mRepo: CategoryRepository? = CategoryRepository.getInstance()
    private var mSubCategoryRepository: SubCategoryRepository? = SubCategoryRepository.getInstance()

    suspend fun addNewCategory(context: Context, categoryModel: CategoryModel) {
        mRepo?.addNewCategory(context, categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {})
    }

    suspend fun updateCategoryList(context: Context, categories: List<CategoryModel>) {
        mRepo?.updateCategoryList(context, categories, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {})
    }

    suspend fun updateCategory(context: Context, categoryModel: CategoryModel) {
        mRepo?.updateCategoryModel(context, categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {})
    }

    suspend fun deleteCategoryItemFromDB(context: Context, categoryModel: CategoryModel) {
        deleteAllSubCategoryForCategoryId(context, categoryModel)
        mRepo?.deleteCategoryItemFromDB(context, categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {
            getCategoriesFromDB(context)
        })
    }

    private suspend fun deleteAllSubCategoryForCategoryId(context: Context, categoryModel: CategoryModel) {
        mSubCategoryRepository?.getSubCategories(context, categoryModel.CategoryId) {
            val arrayList = arrayListOf<SubCategoryListModel>()
            arrayList.addAll(getSubCategories(it))
            CoroutineScope(Dispatchers.IO).launch {
                arrayList.forEach {
                    mSubCategoryRepository?.deleteSubCategoryItemFromDB(context, it,{},{})
                }
            }
        }
    }

    fun getAllSubcategories(context: Context) {
        mSubCategoryRepository?.getAllSubCategoriesFromDB(context) {
            subCategories.clear()
            subCategories.addAll(getSubCategories(it))
        }
    }

    private fun getSubCategories(tables: List<SubcategoryTable>?): List<SubCategoryListModel> {
        val arrayList = arrayListOf<SubCategoryListModel>()
        tables?.forEach {
            getSubCategory(it)?.let { it1 -> arrayList.add(it1) }
        }
        return arrayList.toList()
    }

    private fun getSubCategory(table: SubcategoryTable?): SubCategoryListModel? {
        var model: SubCategoryListModel? = null
        table?.let {
            model = SubCategoryListModel(it.subtaskItemId, it.categoryId, it.subCategoryName, it.subCategoryDescription, it.isTaskDone, it.isImportant, it.priorityId)
        }
        return model
    }

    fun getCategoriesFromDB(context: Context) {
        mRepo = CategoryRepository.getInstance()
        getAllSubcategories(context)
        mRepo?.getCategories(context) {
            CoroutineScope(Dispatchers.Main).launch {
                val arrayList = arrayListOf<CategoryModel>()
                arrayList.addAll(getCategories(it))

                if((subCategories.filter { it.isImportant }).isNotEmpty()) {
                    val importantItem = CategoryModel(0, getString(context, R.string.important), "", 0)
                    arrayList.add(0, importantItem)
                }

                categories.value = arrayList
            }
        }
    }

    private fun getCategories(tables: List<CategoryTable>?): List<CategoryModel> {
        val arrayList = arrayListOf<CategoryModel>()
        tables?.forEach { getCategory(it)?.let { it1 -> arrayList.add(it1) } }
        return arrayList.toList()
    }

    private fun getCategory(table: CategoryTable?): CategoryModel? {
        var model: CategoryModel? = null
        table?.let {
            model = CategoryModel(
                it.id,
                it.CategoryTitle,
                it.CategoryDescription,
                it.priorityId
            )
        }
        return model
    }
}