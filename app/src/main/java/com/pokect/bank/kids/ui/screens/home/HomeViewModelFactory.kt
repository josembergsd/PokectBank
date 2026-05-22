package com.pokect.bank.kids.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pokect.bank.kids.data.repository.AppRepository

import com.pokect.bank.kids.data.repository.home.HomeRepository

class HomeViewModelFactory(
    private val repository: AppRepository,
    private val homeRepository: HomeRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository, homeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
