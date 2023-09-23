package com.aayush.shoppingapp.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aayush.shoppingapp.Repository.SubCategoryRepository
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.SubCategoryListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubCategoryViewModel: ViewModel() {

    private var mRepo: SubCategoryRepository? = SubCategoryRepository.getInstance()

    val subCategories: MutableLiveData<List<SubCategoryListModel>> by lazy {
        MutableLiveData<List<SubCategoryListModel>>()
    }

    val errorModel: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var mCategoryId: Long = 0

    fun addNewSubCategoryItem(context: Context, subtaskListModel: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo?.addNewSubCategoryItem(context, subtaskListModel, onSuccess = {
                getSubCategoriesFromDB(requireContext = context)
            }, onError = {})
        }
    }

    fun updateCategoryList(context: Context, list: List<SubCategoryListModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo?.updateSubCategoryList(context, list, onSuccess = {
                getSubCategoriesFromDB(context)
            }, onError = {})
        }
    }

    fun updateCategoryItem(context: Context, subtaskListModel: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo?.updateSubCategoryItem(context, getSubCategoryTableItem(subtaskListModel),
                onSuccess = {
                    getSubCategoriesFromDB(context)
                }, onError = {
                    errorModel.value = "Something went wrong"
                })
        }
    }

    fun deleteSubCategoryItemFromDB(context: Context, model: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo?.deleteSubCategoryItemFromDB(context, model, onSuccess = {
                getSubCategoriesFromDB(context)
            }, onError = {
                getSubCategoriesFromDB(context)
            })
        }
    }

    fun getSubCategoriesFromDB(requireContext: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo?.getSubCategories(requireContext, mCategoryId) {
                CoroutineScope(Dispatchers.Main).launch {
                    subCategories.value = getSubCategories(it)
                }
            }
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
            model = SubCategoryListModel(it.subtaskItemId, it.categoryId, it.subCategoryName, it.subCategoryDescription, it.isTaskDone, it.isImportant)
        }
        return model
    }

    private fun getSubCategoryTableItem(table: SubCategoryListModel?): SubcategoryTable? {
        var model: SubcategoryTable? = null
        table?.let {
            model = SubcategoryTable(
                it.subtaskItemId, it.categoryId,
                it.subtaskName, it.subtaskDescription,
                it.isTaskDone, it.isImportant)
        }
        return model
    }
}