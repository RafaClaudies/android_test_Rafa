package io.parrotsoftware.qatest.ui.list

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.parrotsoftware.qatest.data.domain.Category
import io.parrotsoftware.qatest.data.domain.Product
import io.parrotsoftware.qatest.data.entities.CategoryEntity
import io.parrotsoftware.qatest.data.entities.ProductEntity
import io.parrotsoftware.qatest.data.repositories.ProductDao
import io.parrotsoftware.qatest.data.repositories.ProductRepository
import io.parrotsoftware.qatest.data.repositories.UserRepository
import io.parrotsoftware.qatest.data.repositories.room.ProductsDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListViewModel : ViewModel(), LifecycleObserver {

    lateinit var userRepository: UserRepository
    lateinit var productRepository: ProductRepository

    private val _viewState = MutableLiveData<ListViewState>()
    fun getViewState() = _viewState

    val isLoading: LiveData<Boolean> = Transformations.map(_viewState) {
        it is ListViewState.Loading
    }

    private var products = mutableListOf<Product>()
    private val categoriesExpanded = mutableMapOf<String, Boolean>()
    private lateinit var dbm: ProductDao


    fun initView(context: Context) {
        fetchProducts(context)
    }

    fun fetchProducts(context: Context) {
        _viewState.value = ListViewState.Loading
        dbm = ProductsDB.getInstance(context)!!.productDao()

        viewModelScope.launch {
            val credentials = userRepository.getCredentials()
            val store = userRepository.getStore()

            if (credentials.isError || store.isError) {
                _viewState.value = ListViewState.ErrorLoadingItems
                return@launch
            }

            val response = productRepository.getProducts(
                credentials.requiredResult.access,
                store.requiredResult.id
            )

            if (response.isError) {
                viewModelScope.launch(Dispatchers.IO) {
                    val listProducts = dbm.getAllCategories()
                    if (listProducts.isNullOrEmpty()) {
                        _viewState.value = ListViewState.ErrorLoadingItems
                    } else {
                        mapProductsBD(listProducts)
                    }
                }
                return@launch
            }

            products = response.requiredResult.toMutableList()
            val expandedCategories = createCategoriesList()
            saveObjects(products)
            _viewState.value = ListViewState.ItemsLoaded(expandedCategories)
        }
    }

    fun updateProduct(productId: String, isAvilable: Boolean) {
        viewModelScope.launch {
            val credentials = userRepository.getCredentials()

            if (credentials.isError) {
                _viewState.value = ListViewState.ErrorUpdatingItem
                return@launch
            }

            val response = productRepository.setProductState(
                credentials.requiredResult.access,
                productId,
                isAvilable
            )

            if (response.isError) {
                _viewState.value = ListViewState.ErrorUpdatingItem
                return@launch
            }

            _viewState.value = ListViewState.ItemUpdated
        }
    }

    fun categorySelected(category: ExpandableCategory) {
        val currentState = categoriesExpanded[category.category.id] ?: false
        categoriesExpanded[category.category.id] = !currentState
        val expandedCategories = createCategoriesList()
        _viewState.value = ListViewState.ItemsLoaded(expandedCategories)
    }

    fun productSelected(product: EnabledProduct) {
        val nextState = product.enabled.not()
        val index = products.indexOfFirst { it.id == product.product.id }
        products[index] = product.product.copy(isAvailable = nextState)
        val expandedCategories = createCategoriesList()
        _viewState.value = ListViewState.ItemsLoaded(expandedCategories)
        updateProduct(product.product.id, nextState)
    }

    private fun createCategoriesList(): List<ExpandableCategory> {
        // Get Categories from products
        val categories = products
            .map { it.category }
            .distinctBy { it.id }
            .sortedBy { it.position }
        val groupedProducts = products.groupBy { it.category.id }

        return categories.map { category ->
            val productGroup = groupedProducts[category.id]?.map { product ->
                EnabledProduct(product, product.isAvailable)
            } ?: emptyList()

            ExpandableCategory(
                category,
                categoriesExpanded[category.id] ?: false,
                productGroup
            )
        }
    }

    private fun saveObjects(listProducts: List<Product>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!dbm.getAllCategories().isNullOrEmpty()) {
                dbm.clearTable()
            }
            listProducts.forEach { product ->
                val categoryEntity = CategoryEntity(product.category.id, product.category.name, product.category.position)
                val productEntity = ProductEntity(product.id, product.name, product.description, product.image, product.price, product.isAvailable, categoryEntity)
                dbm.insert(productEntity)
            }
        }
    }

    private fun mapProductsBD(listProdEnt: MutableList<ProductEntity>) {
        val listProduct: MutableList<Product> = arrayListOf()
        listProdEnt.forEach { productEnt ->
            val category = Category(productEnt.category.idCaEn, productEnt.category.nameCaEn, productEnt.category.position)
            val product = Product(productEnt.id, productEnt.name, productEnt.description, productEnt.image, productEnt.price, productEnt.isAvailable, category)
            listProduct.add(product)
        }
        products = listProduct
        _viewState.postValue(ListViewState.ItemsLoaded(createCategoriesList()))
    }

}