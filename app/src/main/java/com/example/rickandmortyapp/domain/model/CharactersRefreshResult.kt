package com.example.rickandmortyapp.domain.model

data class CharactersRefreshResult(
    val nextPage: Int?,
    val isLastPage: Boolean,
)
