package com.aayush.shoppingapp.use_caseimport com.aayush.shoppingapp.Repository.CategoryRepositoryimport com.aayush.shoppingapp.models.CategoryModelimport javax.inject.Injectclass CategorySaveUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {    suspend fun execute(categoryModel: CategoryModel,                        onSuccess:()->Unit,                        onError:(String?)->Unit) {        categoryRepository.addNewCategory(categoryModel,            onSuccess = {                onSuccess()            }, onError = { message ->                onError(message ?: "Something went Wrong")            })    }}