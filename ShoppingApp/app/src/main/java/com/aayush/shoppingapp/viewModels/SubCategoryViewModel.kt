package com.aayush.shoppingapp.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aayush.shoppingapp.Repository.SubCategoryRepository
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.SubCategoryListModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubCategoryViewModel @Inject constructor(private val mRepo: SubCategoryRepository): ViewModel() {

    val subCategories: MutableLiveData<List<SubCategoryListModel>> by lazy {
        MutableLiveData<List<SubCategoryListModel>>()
    }

    val errorModel: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    var mCategoryId: Long = 0
    var completedTaskHeight = 0;

    fun addNewSubCategoryItem(subtaskListModel: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            if(subtaskListModel.categoryId == 0L) {
                subtaskListModel.isImportant = true
            }
            mRepo.addNewSubCategoryItem(subtaskListModel, onSuccess = {
                getSubCategoriesFromDB()
            }, onError = {})
        }
    }

    fun updateCategoryItem(subtaskListModel: SubCategoryListModel,
                           onSuccess:()->Unit, onFail:()->Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo.updateSubCategoryItem(getSubCategoryTableItem(subtaskListModel),
                onSuccess = {
                    getSubCategoriesFromDB()
                    onSuccess()
                }, onError = {
                    errorModel.value = "Something went wrong"
                    onFail()
                })
        }
    }

    fun deleteSubCategoryItemFromDB(model: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            mRepo.deleteSubCategoryItemFromDB(model, onSuccess = {
                getSubCategoriesFromDB()
            }, onError = {
                getSubCategoriesFromDB()
            })
        }
    }

    fun getSubCategoriesFromDB() {
        if (mCategoryId == 0L || mCategoryId == -1L) {
            mRepo.getAllSubCategoriesFromDB { it ->
                CoroutineScope(Dispatchers.Main).launch {
                    val arrayList = arrayListOf<SubCategoryListModel>()
                    arrayList.clear()
                    if(mCategoryId == 0L) {
                        val list = getSubCategories(it).filter { it.isImportant || it.categoryId == 0L }
                        arrayList.addAll(list)
                    }
                    subCategories.value = arrayList.toList()
                }
            }
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            mRepo.getSubCategories(mCategoryId) {
                CoroutineScope(Dispatchers.Main).launch {
                    val arrayList = arrayListOf<SubCategoryListModel>()
                    getSubCategories(it).forEach {
                        if(it.isImportant) {
                            arrayList.add(0, it)
                        } else {
                            arrayList.add(it)
                        }
                    }
                    subCategories.value = arrayList.toList()
                }
            }
        }
    }

    private fun getSubCategories(tables: List<SubcategoryTable>?): List<SubCategoryListModel> {
        val arrayList = arrayListOf<SubCategoryListModel>()
        tables?.forEach {
            getSubCategory(it)?.let { it1 -> arrayList.add(it1) }
        }
        return arrayList.toList().sortedBy { it.priorityId }
    }

    private fun getSubCategory(table: SubcategoryTable?): SubCategoryListModel? {
        var model: SubCategoryListModel? = null
        table?.let {
            model = SubCategoryListModel(
                it.subtaskItemId,
                it.categoryId,
                it.subCategoryName,
                it.subCategoryDescription,
                it.isTaskDone,
                it.isImportant,
                getSelectedPriorityForTask(it.priorityId),
                it.dueDate,
                it.remind_at
            )
        }
        return model
    }

    private fun getSelectedPriorityForTask(priorityIndex:Int): PRIORITY {
        return if (priorityIndex == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
    }

    private fun getSubCategoryTableItem(table: SubCategoryListModel?): SubcategoryTable? {
        var model: SubcategoryTable? = null
        table?.let {
            model = SubcategoryTable(
                it.subtaskItemId, it.categoryId,
                it.subtaskName, it.subtaskDescription,
                it.isTaskDone, it.isImportant, it.priorityId.value, table.dueDate,table.remindAt)
        }
        return model
    }
}