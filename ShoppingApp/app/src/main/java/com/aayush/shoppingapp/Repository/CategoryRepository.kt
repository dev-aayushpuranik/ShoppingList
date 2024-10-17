package com.aayush.shoppingapp.Repository

import com.aayush.shoppingapp.database.ShoppingDatabase
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepository @Inject constructor(val shoppingDatabase: ShoppingDatabase) {

    fun getCategories(callback: (List<CategoryTable>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.IO) {
                callback(shoppingDatabase.databaseDAO().getAll())
            }
        }
    }

    suspend fun addNewCategory(
        categoryModel: CategoryModel,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        try {
            shoppingDatabase.databaseDAO().insert(getCategoryTable(categoryModel))
            onSuccess()
        } catch (ex: Exception) {
            onError(ex.localizedMessage.toString())
        }
    }

    suspend fun updateCategoryList(
        categoryList: List<CategoryModel>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            shoppingDatabase.databaseDAO().insertAll(getAllCategories(categoryList))
            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun updateCategoryModel(
        categoryModel: CategoryModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            shoppingDatabase.databaseDAO().updateCategory(getCategoryTable(categoryModel))
            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun deleteCategoryItemFromDB(
        categoryModel: CategoryModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            shoppingDatabase.databaseDAO().delete(getCategoryTable(categoryModel))
            onSuccess()
        } catch (ex: Exception) {
            onError()
        }
    }

    private fun getAllCategories(categories: List<CategoryModel>): List<CategoryTable> {
        val list = arrayListOf<CategoryTable>()
        for (category in categories) {
            list.add(getCategoryTable(category))
        }
        return list.toList()
    }

    private fun getCategoryTable(categoryModel: CategoryModel): CategoryTable {
        return CategoryTable(
            categoryModel.CategoryId,
            categoryModel.CategoryName,
            categoryModel.Description,
            categoryModel.priorityId.value
        )
    }
}