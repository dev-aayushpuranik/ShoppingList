package com.aayush.shoppingapp.Repository

import com.aayush.shoppingapp.NetworkState
import com.aayush.shoppingapp.database.ShoppingDatabase
import com.aayush.shoppingapp.database.entities.SubcategoryTable
import com.aayush.shoppingapp.models.SubCategoryListModel
import javax.inject.Inject

class SubCategoryRepository @Inject constructor(val shoppingDatabase: ShoppingDatabase) {

    suspend fun getSubCategories(categoryId: Long): NetworkState<List<SubcategoryTable>?>  {
        return try {
            NetworkState.Success(shoppingDatabase.databaseDAO().getAll(categoryId)?.sortedBy { it.priorityId })
        } catch (ex: Exception) {
            NetworkState.Error(ex.message)
        }
    }

    suspend fun getAllSubCategoriesFromDB(): NetworkState<List<SubcategoryTable>?> {
        return try {
            NetworkState.Success(shoppingDatabase.databaseDAO().getAllSubCategories()?.sortedBy { it.priorityId })
        }catch (ex: Exception) {
            NetworkState.Error("Error")
        }
    }

    suspend fun addNewSubCategoryItem(subCategoryListModel: SubCategoryListModel): NetworkState<List<SubCategoryListModel>?> {
        return try {
            shoppingDatabase.databaseDAO().insert(getSubCategoryTable(subCategoryListModel))
            NetworkState.Success(null)
        } catch (ex: Exception) {
            NetworkState.Error("Error")
        }
    }

    suspend fun updateSubCategoryList(
        subCategoryList: List<SubCategoryListModel>
    ): NetworkState<List<SubCategoryListModel>?> {
        return try {
            shoppingDatabase.databaseDAO().insertAll(getAllSubCategories(subCategoryList))
            NetworkState.Success(null)
        } catch (ex: java.lang.Exception) {
            NetworkState.Error("Error")
        }
    }

    suspend fun updateSubCategoryItem(
        subCategoryTable: SubcategoryTable?,
    ): NetworkState<List<SubCategoryListModel>?> {
        return try {
            if (subCategoryTable != null) {
                shoppingDatabase.databaseDAO().updateTaskDoneForSubCategory(subCategoryTable)
                NetworkState.Success(null)
            } else {
                NetworkState.Error("Error")
            }
        } catch (ex: java.lang.Exception) {
            NetworkState.Error("Error")
        }
    }

    suspend fun deleteSubCategoryItemFromDB(
        subCategoryListModel: SubCategoryListModel
    ): NetworkState<List<SubcategoryTable>?> {
        return try {
            shoppingDatabase.databaseDAO().delete(getSubCategoryTable(subCategoryListModel))
            NetworkState.Success(null)
        } catch (ex: Exception) {
            NetworkState.Error("Error")
        }
    }

    private fun getAllSubCategories(subCategories: List<SubCategoryListModel>): List<SubcategoryTable> {
        val list = arrayListOf<SubcategoryTable>()
        for (category in subCategories) {
            list.add(getSubCategoryTable(category))
        }
        return list.toList().sortedBy { it.priorityId }
    }

    private fun getSubCategoryTable(model: SubCategoryListModel): SubcategoryTable {
        return SubcategoryTable(
            model.subtaskItemId,
            model.categoryId,
            model.subtaskName,
            model.subtaskDescription,
            model.isTaskDone,
            model.isImportant,
            model.priorityId.value,
            model.dueDate,
            model.remindAt
        )
    }
}