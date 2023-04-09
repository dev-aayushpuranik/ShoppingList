package com.aayush.shoppingapp.Repository

import android.content.Context
import com.aayush.shoppingapp.common.extensions.orDefault
import com.aayush.shoppingapp.database.SubCategoryDatabase
import com.aayush.shoppingapp.database.entities.SubcategoryTable
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

    fun getSubCategories(context: Context, callback:(List<SubcategoryTable>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val subCategoryDatabase by lazy { SubCategoryDatabase.getDatabase(context).SubcategoryDao() }

            withContext(Dispatchers.IO) { callback(subCategoryDatabase.getAll().orDefault()) }
        }
    }
}