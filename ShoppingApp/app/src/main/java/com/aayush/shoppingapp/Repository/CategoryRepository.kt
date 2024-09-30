package com.aayush.shoppingapp.Repository

import android.content.Context
import com.aayush.shoppingapp.database.ShoppingDatabase
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import kotlinx.coroutines.*
import javax.inject.Inject

class CategoryRepository @Inject constructor() {

    fun getCategories(context: Context, callback: (List<CategoryTable>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val categoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }

            withContext(Dispatchers.IO ) {
                callback(categoryDatabase.getAll())
            }
        }
    }

    suspend fun addNewCategory(
        context: Context,
        categoryModel: CategoryModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val categoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }
            categoryDatabase.insert(getCategoryTable(categoryModel))
            onSuccess.invoke()
        } catch (ex: Exception) {
            onError.invoke()
        }
    }

    suspend fun updateCategoryList(
        context: Context,
        categoryList: List<CategoryModel>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val categoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }
            categoryDatabase.insertAll(getAllCategories(categoryList))
            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun updateCategoryModel(
        context: Context,
        categoryModel: CategoryModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val categoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO() }
            categoryDatabase.updateCategory(getCategoryTable(categoryModel))
            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    suspend fun deleteCategoryItemFromDB(context: Context,
                                                categoryModel: CategoryModel,
                                                onSuccess: () -> Unit,
                                                onError: () -> Unit) {
        try {
            val categoryDatabase by lazy { ShoppingDatabase.getDatabase(context).databaseDAO()}
            categoryDatabase.delete(getCategoryTable(categoryModel))
            onSuccess()
        }catch (ex:Exception) {
            onError()
        }
    }

    private fun getAllCategories(categories: List<CategoryModel>) : List<CategoryTable> {
        val list = arrayListOf<CategoryTable>()
        for (category in categories) {
            list.add(getCategoryTable(category))
        }
        return list.toList()
    }

    private fun getCategoryTable(categoryModel: CategoryModel) : CategoryTable {
        return CategoryTable(categoryModel.CategoryId,categoryModel.CategoryName,categoryModel.Description, categoryModel.priorityId.value)
    }
}