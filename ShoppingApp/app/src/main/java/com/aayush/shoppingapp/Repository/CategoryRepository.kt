package com.aayush.shoppingapp.Repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aayush.shoppingapp.database.CategoryDatabase
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import kotlinx.coroutines.*

class CategoryRepository {

    companion object {
        var instance: CategoryRepository? = null

        @JvmName("getInstance1")
        fun getInstance(): CategoryRepository? {
            if(instance == null) {
                instance = CategoryRepository()
            }

            return instance
        }
    }

    fun getCategories(context: Context, callback: (List<CategoryTable>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val categoryDatabase by lazy { CategoryDatabase.getDatabase(context).CategoryDao() }

            withContext(Dispatchers.IO ) {
                callback(categoryDatabase.getAll())
            }
        }
    }

    public suspend fun addNewCategory(
        context: Context,
        categoryModel: CategoryModel,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        try {
            val categoryDatabase by lazy { CategoryDatabase.getDatabase(context).CategoryDao() }

            categoryDatabase.insert(getCategoryTable(categoryModel))

            onSuccess.invoke()
        } catch (ex: java.lang.Exception) {
            onError.invoke()
        }
    }

    private fun getCategoryTable(categoryModel: CategoryModel) : CategoryTable {
       return CategoryTable(categoryModel.CategoryId,categoryModel.CategoryName,categoryModel.Description)
    }
}