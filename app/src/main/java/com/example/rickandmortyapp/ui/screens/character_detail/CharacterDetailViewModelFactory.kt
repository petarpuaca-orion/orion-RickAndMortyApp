package com.example.rickandmortyapp.ui.screens.character_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rickandmortyapp.domain.usecase.character.GetCharacterDetailUseCase

class CharacterDetailViewModelFactory(
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase,
    private val characterId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterDetailViewModel::class.java)) {
            return CharacterDetailViewModel(
                getCharacterDetailUseCase = getCharacterDetailUseCase,
                characterId = characterId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}