package com.rsl.youresto.di

import com.rsl.youresto.ui.main_login.MainLoginViewModel
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.youresto.ui.main_screen.favorite_items.NewFavoriteItemViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.youresto.ui.main_screen.order_history.NewOrdersViewModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.NewTablesViewModel
import com.rsl.youresto.ui.server_login.ServerLoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainLoginViewModel(get()) }
    viewModel { NewTablesViewModel(get()) }
    viewModel { NewProductViewModel(get()) }
    viewModel { NewCartViewModel(get()) }
    viewModel { SharedCheckoutViewModel() }
    viewModel { NewOrdersViewModel(get()) }
    viewModel { NewFavoriteItemViewModel(get()) }
    viewModel { ServerLoginViewModel(get()) }
}
