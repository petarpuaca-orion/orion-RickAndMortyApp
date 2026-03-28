package com.example.rickandmortyapp.domain.model

sealed interface CharacterDetailResult {
    data object Loading : CharacterDetailResult
    data class Success(val character: CharacterModel?) : CharacterDetailResult
    data class Error(val message: String) : CharacterDetailResult
}