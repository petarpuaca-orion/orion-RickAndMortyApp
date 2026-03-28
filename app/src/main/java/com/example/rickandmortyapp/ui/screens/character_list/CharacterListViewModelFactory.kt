package com.example.rickandmortyapp.ui.screens.character_list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rickandmortyapp.domain.usecase.character.GetCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.LoadMoreCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.RefreshCharactersUseCase

class CharacterListViewModelFactory(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val refreshCharactersUseCase: RefreshCharactersUseCase,
    private val loadMoreCharactersUseCase: LoadMoreCharactersUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterListViewModel::class.java)) {
            return CharacterListViewModel(
                getCharactersUseCase = getCharactersUseCase,
                refreshCharactersUseCase = refreshCharactersUseCase,
                loadMoreCharactersUseCase = loadMoreCharactersUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}