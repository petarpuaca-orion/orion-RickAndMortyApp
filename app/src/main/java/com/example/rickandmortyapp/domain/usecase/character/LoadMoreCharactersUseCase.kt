package com.example.rickandmortyapp.domain.usecase.character

import com.example.rickandmortyapp.domain.model.CharactersRefreshResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository

class LoadMoreCharactersUseCase(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(page: Int): CharactersRefreshResult {
        return repository.refreshCharacters(page)
    }
}
