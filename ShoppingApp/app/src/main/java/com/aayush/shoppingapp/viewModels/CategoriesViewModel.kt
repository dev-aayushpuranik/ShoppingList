package com.aayush.shoppingapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aayush.shoppingapp.NetworkState
import com.aayush.shoppingapp.Repository.CategoryRepository
import com.aayush.shoppingapp.Repository.SubCategoryRepository
import com.aayush.shoppingapp.UIState
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.common.extensions.orDefaultStr
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.views.ToDoItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor() : ViewModel() {
    private val _categoryStateFlow = MutableStateFlow<UIState<List<CategoryModel>?>>(UIState.Loading)
    val CategoryStateFlow: StateFlow<UIState<List<CategoryModel>?>> = _categoryStateFlow.asStateFlow()
    val toDoItemState = MutableStateFlow(ToDoItemState())
    val isUndoPressedForDelete = MutableStateFlow(false)
    @Inject
    lateinit var mCategoryRepository: CategoryRepository
    @Inject
    lateinit var mSubCategoryRepository: SubCategoryRepository
    private var recentlyDeletedCategoryModel: CategoryModel? = null
    suspend fun getCategoriesFromDB() {
        _categoryStateFlow.value = UIState.Loading
        val result: NetworkState<List<CategoryModel>?> = mCategoryRepository.getCategories()
        return when(result) {
            is NetworkState.Error -> setErrorMessage(result.message.orDefaultStr())
            is NetworkState.Success -> _categoryStateFlow.value = UIState.Success(result.data)
        }
    }

    fun addNewCategory() {
        val categoryModel = CategoryModel(
            System.currentTimeMillis(),
            toDoItemState.value.categoryName,
            toDoItemState.value.categoryDescription,
            toDoItemState.value.priority
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _categoryStateFlow.value = UIState.Loading
                toDoItemState.update {
                    it.copy(
                        categoryName = "",
                        categoryDescription = ""
                    )
                }
                val result = mCategoryRepository.addNewCategory(categoryModel)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkState.Error -> setErrorMessage(result.message)
                        is NetworkState.Success -> getCategoriesFromDB()
                    }
                }
            }
        }
    }

    fun updateCategory(categoryModel: CategoryModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _categoryStateFlow.value = UIState.Loading
                val result = mCategoryRepository.updateCategoryModel(categoryModel)
                when (result) {
                    is NetworkState.Error -> setErrorMessage(result.message)
                    is NetworkState.Success -> getCategoriesFromDB()
                }
            }
        }
    }

    fun undoDelete() {
        isUndoPressedForDelete.value = true
    }

    fun deleteCategoryItemFromDB(categoryModel: CategoryModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                delay(1000)
                if(!isUndoPressedForDelete.value) {
                    deleteAllSubCategoryForCategoryId(categoryModel)

                    val result = mCategoryRepository.deleteCategoryItemFromDB(categoryModel)
                    isUndoPressedForDelete.value = false
                    when (result) {
                        is NetworkState.Error -> setErrorMessage(result.message)
                        is NetworkState.Success -> getCategoriesFromDB()
                    }
                } else {
                    isUndoPressedForDelete.value = false
                    _categoryStateFlow.value = UIState.NOTHING
                }
            }
        }
    }

    private suspend fun deleteAllSubCategoryForCategoryId(categoryModel: CategoryModel) {
        val result = mSubCategoryRepository.getSubCategories(categoryModel.CategoryId)
        when(result) {
            is NetworkState.Error -> setErrorMessage(result.message)
            is NetworkState.Success -> {
                val arrayList = arrayListOf<SubCategoryListModel>()
                arrayList.addAll(getSubCategories(result.data))

                arrayList.forEach {
                    mSubCategoryRepository.deleteSubCategoryItemFromDB(it)
                }
            }
        }
    }

    private fun getSubCategories(tables: List<SubcategoryTable>?): List<SubCategoryListModel> {
        val arrayList = arrayListOf<SubCategoryListModel>()
        tables?.forEach { item -> getSubCategory(item)?.let { it1 -> arrayList.add(it1) } }
        return arrayList.toList()
    }

    private fun getSubCategory(table: SubcategoryTable): SubCategoryListModel? {
        var model: SubCategoryListModel? = null
        table.let {
            model = SubCategoryListModel(
                it.subtaskItemId,
                it.categoryId, it.subCategoryName,
                it.subCategoryDescription, it.isTaskDone, it.isImportant,
                getSelectedPriorityForTask(it.priorityId), it.dueDate, it.remind_at
            )
        }
        return model
    }

    private fun getSelectedPriorityForTask(priorityIndex: Int): PRIORITY {
        return if (priorityIndex == PRIORITY.HIGH.value) PRIORITY.HIGH
        else if (priorityIndex == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM
        else PRIORITY.LOW
    }

    private fun setErrorMessage(message: String?) {
        _categoryStateFlow.value = UIState.Error(message.orDefaultStr())
    }
}