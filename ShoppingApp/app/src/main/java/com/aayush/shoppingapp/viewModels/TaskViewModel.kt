package com.aayush.shoppingapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aayush.shoppingapp.NetworkState
import com.aayush.shoppingapp.Repository.SubCategoryRepository
import com.aayush.shoppingapp.UIState
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.views.ToDoTaskItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(private val mRepo: SubCategoryRepository) :
    ViewModel() {

    private val _subCategoryStateFlow =
        MutableStateFlow<UIState<List<SubCategoryListModel>>>(UIState.Loading)
    val SubCategoryStateFlow: StateFlow<UIState<List<SubCategoryListModel>?>> =
        _subCategoryStateFlow.asStateFlow()
    val toDoTaskItemState = MutableStateFlow(ToDoTaskItemState())
    var mCategoryId: Long = 0
    var completedTaskHeight = 0

    fun addNewSubCategoryItem(subtaskListModel: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            if (subtaskListModel.categoryId == 0L) {
                subtaskListModel.isImportant = true
            }
            val result = mRepo.addNewSubCategoryItem(subtaskListModel)
            when (result) {
                is NetworkState.Error -> setErrorMessage(result.message)
                is NetworkState.Success<*> -> getSubCategoriesFromDB()
            }
        }
    }

    fun updateCategoryItem(subtaskListModel: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = mRepo.updateSubCategoryItem(getSubCategoryTableItem(subtaskListModel))
            when (result) {
                is NetworkState.Error -> setErrorMessage(result.message)
                is NetworkState.Success<*> -> getSubCategoriesFromDB()
            }
        }
    }

    fun deleteSubCategoryItemFromDB(model: SubCategoryListModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = mRepo.deleteSubCategoryItemFromDB(model)
            when (result) {
                is NetworkState.Error -> {
                    setErrorMessage(result.message)
                    getSubCategoriesFromDB()
                }

                is NetworkState.Success<*> -> getSubCategoriesFromDB()
            }
        }
    }

    fun getSubCategoriesFromDB() {
        if (mCategoryId == 0L || mCategoryId == -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = mRepo.getAllSubCategoriesFromDB()
                when(result) {
                    is NetworkState.Error -> setErrorMessage(result.message)
                    is NetworkState.Success<*> -> {
                        withContext(Dispatchers.Main) {
                            try {
                                val arrayList = arrayListOf<SubCategoryListModel>()
                                arrayList.clear()
                                if (mCategoryId == 0L) {
                                    val list =
                                        getSubCategories(result.data as List<SubcategoryTable>).filter { it.isImportant || it.categoryId == 0L }
                                    arrayList.addAll(list)
                                }
                                _subCategoryStateFlow.value = UIState.Success(arrayList)
                            } catch (ex: Exception) {
                                _subCategoryStateFlow.value = UIState.NOTHING
                            }
                        }
                    }
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                val result = mRepo.getSubCategories(mCategoryId)
                when (result) {
                    is NetworkState.Error -> setErrorMessage(result.message)
                    is NetworkState.Success<*> -> {
                        try {
                            val arrayList = arrayListOf<SubCategoryListModel>()
                            getSubCategories(result.data as List<SubcategoryTable>).forEach {
                                if (it.isImportant) {
                                    arrayList.add(0, it)
                                } else {
                                    arrayList.add(it)
                                }
                            }
                            _subCategoryStateFlow.value = UIState.Success(arrayList)
                        } catch (ex: Exception) {
                            _subCategoryStateFlow.value = UIState.NOTHING
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val job = CoroutineScope(Dispatchers.IO).async {
                    mRepo.getSubCategories(mCategoryId)
                }
                val result = job.await()
                when(result) {
                    is NetworkState.Error -> setErrorMessage(result.message)
                    is NetworkState.Success<*> -> {
                        try {
                            val arrayList = arrayListOf<SubCategoryListModel>()
                            getSubCategories(result.data as List<SubcategoryTable>).forEach {
                                if (it.isImportant) {
                                    arrayList.add(0, it)
                                } else {
                                    arrayList.add(it)
                                }
                            }
                            _subCategoryStateFlow.value = UIState.Success(arrayList)
                        } catch (ex: Exception) {
                            _subCategoryStateFlow.value = UIState.NOTHING
                        }
                    }
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

    private fun getSelectedPriorityForTask(priorityIndex: Int): PRIORITY {
        return if (priorityIndex == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
    }

    private fun getSubCategoryTableItem(table: SubCategoryListModel?): SubcategoryTable? {
        var model: SubcategoryTable? = null
        table?.let {
            model = SubcategoryTable(
                it.subtaskItemId,
                it.categoryId,
                it.subtaskName,
                it.subtaskDescription,
                it.isTaskDone,
                it.isImportant,
                it.priorityId.value,
                table.dueDate,
                table.remindAt
            )
        }
        return model
    }

    private fun setErrorMessage(message: String?) {
        UIState.Error(message ?: "Something Went Wrong. Please try again.")
    }
}