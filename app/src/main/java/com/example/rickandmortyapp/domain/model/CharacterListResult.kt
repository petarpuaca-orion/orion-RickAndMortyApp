package com.example.rickandmortyapp.domain.model

sealed interface CharacterListResult {
    data object Loading : CharacterListResult

    data class Success(
        val characters: List<CharacterModel>,
        val nextPage: Int?,
        val isLastPage: Boolean,
    ) : CharacterListResult

    data class Error(val message: String) : CharacterListResult
}
