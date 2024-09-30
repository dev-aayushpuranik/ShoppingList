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

class SubCategoryRepository @Inject constructor() {

    fun getSubCategories(context: Context, categoryId: Long , callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val db by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }

            withContext(Dispatchers.IO) { callback(db.getAll(categoryId)?.sortedBy { it.priorityId }.orDefault()) }
        }
    }

    fun getAllSubCategoriesFromDB(context: Context, callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val db by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }

            withContext(Dispatchers.IO) { callback(db.getAllSubCategories()?.sortedBy { it.priorityId }.orDefault()) }
        }
    }

    suspend fun addNewSubCategoryItem(
        context: Context,
        subCategoryListModel: SubCategoryListModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val db by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }

            db.insert(getSubCategoryTable(subCategoryListModel))

            onSuccess.invoke()
        } catch (ex: Exception) {
            onError.invoke()
        }
    }

    suspend fun updateSubCategoryList(
        context: Context,
        subCategoryList: List<SubCategoryListModel>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val db by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }
            db.insertAll(getAllSubCategories(subCategoryList))
            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun updateSubCategoryItem(
        context: Context,
        subCategoryTable: SubcategoryTable?,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val subCategoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }
            if (subCategoryTable != null) {
                subCategoryDatabase.updateTaskDoneForSubCategory(subCategoryTable)
                onSuccess.invoke()
            } else {
                onError()
            }
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun deleteSubCategoryItemFromDB(context: Context,
                                         subCategoryListModel: SubCategoryListModel,
                                         onSuccess: () -> Unit,
                                         onError: () -> Unit) {
        try {
            val subCategoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO()}
            subCategoryDatabase.delete(getSubCategoryTable(subCategoryListModel))
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