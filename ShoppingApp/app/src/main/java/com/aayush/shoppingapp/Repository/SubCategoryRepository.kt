package com.aayush.shoppingapp.Repository

import android.content.Context
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.database.SubCategoryDatabase
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.SubCategoryListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubCategoryRepository {

    companion object {
        private var instance: SubCategoryRepository? = null

        @JvmName("getInstance1")
        fun getInstance(): SubCategoryRepository? {
            if(instance == null) { instance = SubCategoryRepository() }
            return instance
        }
    }

    fun getSubCategories(context: Context, categoryId: Long , callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao() }

            withContext(Dispatchers.IO) { callback(subCategoryDatabase.getAll(categoryId).orDefault()) }
        }
    }

    fun getAllSubCategoriesFromDB(context: Context, callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao() }

            withContext(Dispatchers.IO) { callback(subCategoryDatabase.getAllSubCategories().orDefault()) }
        }
    }

    suspend fun addNewSubCategoryItem(
        context: Context,
        subCategoryListModel: SubCategoryListModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao() }

            subCategoryDatabase.insert(getSubCategoryTable(subCategoryListModel))

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
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao() }

            subCategoryDatabase.insertAll(getAllSubCategories(subCategoryList))

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
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao() }
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
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao()}
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
        return list.toList()
    }

    private fun getSubCategoryTable(model: SubCategoryListModel) : SubcategoryTable {
        return SubcategoryTable(model.subtaskItemId, model.categoryId, model.subtaskName, model.subtaskDescription, model.isTaskDone, model.isImportant)
    }
}