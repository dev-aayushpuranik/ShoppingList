package com.aayush.shoppingapp.Repository

import android.content.Context
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.database.ShoppingDatabase
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.SubCategoryListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubCategoryRepository @Inject constructor(val shoppingDatabase: ShoppingDatabase) {

    fun getSubCategories(categoryId: Long , callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.IO) { callback(shoppingDatabase.databaseDAO().getAll(categoryId)?.sortedBy { it.priorityId }.orDefault()) }
        }
    }

    fun getAllSubCategoriesFromDB(callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) { callback(shoppingDatabase.databaseDAO().getAllSubCategories()?.sortedBy { it.priorityId }.orDefault()) }
        }
    }

    suspend fun addNewSubCategoryItem(
        subCategoryListModel: SubCategoryListModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            shoppingDatabase.databaseDAO().insert(getSubCategoryTable(subCategoryListModel))
            onSuccess.invoke()
        } catch (ex: Exception) {
            onError.invoke()
        }
    }

    suspend fun updateSubCategoryList(
        subCategoryList: List<SubCategoryListModel>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            shoppingDatabase.databaseDAO().insertAll(getAllSubCategories(subCategoryList))
            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun updateSubCategoryItem(
        subCategoryTable: SubcategoryTable?,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            if (subCategoryTable != null) {
                shoppingDatabase.databaseDAO().updateTaskDoneForSubCategory(subCategoryTable)
                onSuccess.invoke()
            } else {
                onError()
            }
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun deleteSubCategoryItemFromDB(
                                         subCategoryListModel: SubCategoryListModel,
                                         onSuccess: () -> Unit,
                                         onError: () -> Unit) {
        try {
            shoppingDatabase.databaseDAO().delete(getSubCategoryTable(subCategoryListModel))
            onSuccess()
        }catch (ex:Exception) {
            onError()
        }
    }

    private fun getAllSubCategories(subCategories: List<SubCategoryListModel>) : List<SubcategoryTable> {
        val list = arrayListOf<SubcategoryTable>()
        for (category in subCategories) {
            list.add(getSubCategoryTable(category))
        }
        return list.toList().sortedBy { it.priorityId }
    }

    private fun getSubCategoryTable(model: SubCategoryListModel) : SubcategoryTable {
        return SubcategoryTable(model.subtaskItemId, model.categoryId, model.subtaskName, model.subtaskDescription, model.isTaskDone, model.isImportant, model.priorityId.value,model.dueDate,model.remindAt)
    }
}