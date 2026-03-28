package com.example.rickandmortyapp.domain.usecase.character

import com.example.rickandmortyapp.domain.model.CharactersRefreshResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository

class RefreshCharactersUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(): CharactersRefreshResult {
        return repository.refreshCharacters(page = 1)
    }
}