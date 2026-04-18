package com.aayush.shoppingapp.Repository

import com.aayush.shoppingapp.NetworkState
import com.aayush.shoppingapp.common.Enums.PRIORITY
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.database.ShoppingDatabase
import com.aayush.shoppingapp.database.entities.CategoryTable
import com.aayush.shoppingapp.models.CategoryModel
import javax.inject.Inject

class CategoryRepository @Inject constructor(val shoppingDatabase: ShoppingDatabase) {

    suspend fun getCategories(): NetworkState<List<CategoryModel>?> {
        return try {
            NetworkState.Success(getAllCategoryModelList(shoppingDatabase.databaseDAO().getAll()))
        } catch (ex: Exception) {
            NetworkState.Error(ex.message ?: "Something Went Wrong. Please try again.")
        }
    }

    suspend fun addNewCategory(categoryModel: CategoryModel): NetworkState<List<CategoryModel>?> {
        return try {
            shoppingDatabase.databaseDAO().insert(getCategoryTable(categoryModel))
            NetworkState.Success(null)
        } catch (ex: Exception) {
            return NetworkState.Error(ex.message)
        }
    }

    suspend fun updateCategoryList(
        categoryList: List<CategoryModel>
    ): NetworkState<List<CategoryModel>?> {
        return try {
            shoppingDatabase.databaseDAO().insertAll(getAllCategories(categoryList))
            NetworkState.Success(null)
        } catch (ex: Exception) {
            NetworkState.Error(ex.message)
        }
    }

    suspend fun updateCategoryModel(categoryModel: CategoryModel): NetworkState<List<CategoryModel>?> {
        return try {
            shoppingDatabase.databaseDAO().updateCategory(getCategoryTable(categoryModel))
            NetworkState.Success(null)
        } catch (ex: java.lang.Exception) {
            NetworkState.Error(ex.message)
        }
    }

    suspend fun deleteCategoryItemFromDB(categoryModel: CategoryModel): NetworkState<List<CategoryModel>?> {
        try {
            shoppingDatabase.databaseDAO().delete(getCategoryTable(categoryModel))
            return NetworkState.Success(null)
        } catch (ex: Exception) {
            return NetworkState.Error(ex.message)
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

    private fun getAllCategoryModelList(categories: List<CategoryTable>?): List<CategoryModel> {
        val list = arrayListOf<CategoryModel>()
        for (category in categories.orDefault()) { list.add(getCategoryModel(category)) }
        return list.toList()
    }

    private fun getCategoryModel(ct: CategoryTable): CategoryModel {
        return CategoryModel(
            ct.id,
            ct.CategoryTitle,
            ct.CategoryDescription,
            PRIORITY.entries.get(ct.priorityId)
        )
    }
}