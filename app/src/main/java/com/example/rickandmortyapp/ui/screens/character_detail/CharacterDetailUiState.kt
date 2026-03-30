package com.example.rickandmortyapp.ui.screens.character_detail

import com.example.rickandmortyapp.domain.model.CharacterModel

data class CharacterDetailUiState(
    val isLoading: Boolean = false,
    val character: CharacterModel? = null,
    val errorMessage: String? = null,
)
