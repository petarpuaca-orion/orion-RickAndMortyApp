package com.example.rickandmortyapp.domain.repository

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharactersRefreshResult
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeCharacters(): Flow<List<CharacterModel>>

    suspend fun refreshCharacters(page: Int = 1): CharactersRefreshResult

    fun observeCharacter(id: Int): Flow<CharacterModel?>

    suspend fun refreshCharacter(id: Int)
}
