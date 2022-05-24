package io.parrotsoftware.qatest.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.parrotsoftware.qa_network.interactors.impl.NetworkInteractorImpl
import io.parrotsoftware.qatest.R
import io.parrotsoftware.qatest.databinding.FragmentListBinding
import io.parrotsoftware.qatest.common.observe
import io.parrotsoftware.qatest.common.toast
import io.parrotsoftware.qatest.data.managers.impl.UserManagerImpl
import io.parrotsoftware.qatest.data.repositories.impl.ProductRepositoryImpl
import io.parrotsoftware.qatest.data.repositories.impl.UserRepositoryImpl


class ListFragment :
    Fragment(),
    CategoryListener {

    private lateinit var viewModel: ListViewModel
    private lateinit var binding: FragmentListBinding

    private val categoryController by lazy {
        CategoryController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(ListViewModel::class.java)

        // TODO Inject
        viewModel.userRepository = UserRepositoryImpl(
            UserManagerImpl(requireContext()),
            NetworkInteractorImpl()
        )
        viewModel.productRepository = ProductRepositoryImpl(NetworkInteractorImpl())

        binding.viewModel = viewModel

        lifecycle.addObserver(viewModel)
        observe(viewModel.getViewState(), ::onViewState)

        binding.recyclerProducts.adapter = categoryController.adapter
        binding.swipeProducts.setOnRefreshListener { viewModel.fetchProducts(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initView(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.logout_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                TODO("Implement")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onViewState(state: ListViewState?) {
        when (state) {
            ListViewState.ErrorLoadingItems -> {
                requireContext().toast("Error al cargar los productos")
            }
            is ListViewState.ItemsLoaded -> {
                categoryController.categories = state.categories
            }
            ListViewState.ErrorUpdatingItem -> {
                requireContext().toast("Error al actualizar el producto")
            }
            else -> {}
        }
    }

    override fun onCategorySelected(category: ExpandableCategory) {
        viewModel.categorySelected(category)
    }

    override fun onProductSelected(product: EnabledProduct) {
        viewModel.productSelected(product)
    }
}