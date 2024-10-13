package com.aayush.shoppingapp.viewModels

import android.content.Context
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.Repository.CategoryRepository
import com.aayush.shoppingapp.Repository.SubCategoryRepository
import com.aayush.shoppingapp.UIState
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import com.aayush.shoppingapp.models.SubCategoryListModel
import com.aayush.shoppingapp.use_case.CategorySaveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor() : ViewModel() {

    val uiStateUpdate: MutableLiveData<UIState<String>> by lazy {
        MutableLiveData<UIState<String>>()
    }

    val categories: MutableLiveData<List<CategoryModel>> by lazy {
        MutableLiveData<List<CategoryModel>>()
    }

    val subCategories: ArrayList<SubCategoryListModel> by lazy {
        arrayListOf()
    }

    @Inject
    lateinit var repository: CategoryRepository

    @Inject
    lateinit var mSubCategoryRepository: SubCategoryRepository

    fun addNewCategory(categoryModel: CategoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val state = CategorySaveUseCase(repository).execute(categoryModel)
            withContext(Dispatchers.Main) {
                when (state) {
                    is UIState.Error -> {
                        uiStateUpdate.value = UIState.Error("Something went wrong")
                        uiStateUpdate.value = UIState.IdleState
                    }

                    UIState.Loading -> {
                        uiStateUpdate.value = UIState.IdleState
                    }

                    is UIState.Success -> {
                        uiStateUpdate.value = UIState.Success("Saved Successfully")
                        uiStateUpdate.value = UIState.IdleState
                    }
                    UIState.IdleState -> {

                    }
                }
            }
        }
    }

    suspend fun updateCategory(context: Context, categoryModel: CategoryModel) {
        repository.updateCategoryModel(categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {})
    }

    suspend fun deleteCategoryItemFromDB(context: Context, categoryModel: CategoryModel) {
        deleteAllSubCategoryForCategoryId(context, categoryModel)
        repository.deleteCategoryItemFromDB(context, categoryModel, onSuccess = {
            getCategoriesFromDB(context)
        }, onError = {
            getCategoriesFromDB(context)
        })
    }

    private suspend fun deleteAllSubCategoryForCategoryId(context: Context, categoryModel: CategoryModel) {
        mSubCategoryRepository.getSubCategories(categoryModel.CategoryId) {
            val arrayList = arrayListOf<SubCategoryListModel>()
            arrayList.addAll(getSubCategories(it))
            CoroutineScope(Dispatchers.IO).launch {
                arrayList.forEach {
                    mSubCategoryRepository.deleteSubCategoryItemFromDB(it,{},{})
                }
            }
        }
    }

    fun getAllSubcategories(context: Context) {
        mSubCategoryRepository.getAllSubCategoriesFromDB {
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
            model = SubCategoryListModel(it.subtaskItemId, it.categoryId, it.subCategoryName, it.subCategoryDescription, it.isTaskDone, it.isImportant, getSelectedPriorityForTask(it.priorityId), it.dueDate,it.remind_at)
        }
        return model
    }

    private fun getSelectedPriorityForTask(priorityIndex:Int): PRIORITY {
        return if (priorityIndex == PRIORITY.HIGH.value) PRIORITY.HIGH else if (priorityIndex == PRIORITY.MEDIUM.value) PRIORITY.MEDIUM else PRIORITY.LOW
    }

    fun getCategoriesFromDB(context: Context) {
        getAllSubcategories(context)
        repository.getCategories {
            CoroutineScope(Dispatchers.Main).launch {
                val arrayList = arrayListOf<CategoryModel>()
                arrayList.addAll(getCategories(it))
                arrayList.sortBy { it.priorityId }

                if((subCategories.filter { it.isImportant }).isNotEmpty()) {
                    val importantItem = CategoryModel(0, getString(context, R.string.important), "", PRIORITY.HIGH)
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
            val priority = when(it.priorityId) {
                PRIORITY.HIGH.value -> PRIORITY.HIGH
                PRIORITY.MEDIUM.value -> PRIORITY.MEDIUM
                PRIORITY.LOW.value -> PRIORITY.LOW
                else -> { PRIORITY.LOW}
            }
            model = CategoryModel(
                it.id,
                it.CategoryTitle,
                it.CategoryDescription,
                priority
            )
        }
        return model
    }
}