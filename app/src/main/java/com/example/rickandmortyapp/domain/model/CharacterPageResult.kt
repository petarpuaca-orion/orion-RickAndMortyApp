package com.example.rickandmortyapp.domain.model

data class CharacterPageResult(
    val characters: List<CharacterModel>,
    val nextPage: Int?,
    val isLastPage: Boolean
)
