package com.example.rickandmortyapp.ui.screens.character_list

import com.example.rickandmortyapp.domain.model.CharacterModel

data class CharacterListUiState(
    val characters: List<CharacterModel> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val endReached: Boolean = false
)
