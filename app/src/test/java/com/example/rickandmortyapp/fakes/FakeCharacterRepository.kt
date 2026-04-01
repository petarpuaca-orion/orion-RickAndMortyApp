package com.example.rickandmortyapp.fakes

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharactersRefreshResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeCharacterRepository : CharacterRepository {
    val observeCharactersFlow = MutableSharedFlow<List<CharacterModel>>(replay = 1)
    val observeCharacterByIdFlow = MutableSharedFlow<CharacterModel?>(replay = 1)

    var refreshCharactersResult: Result<CharactersRefreshResult>? = null
    var refreshCharacterResult: Result<Unit>? = null

    var refreshCharactersCallCount = 0
    var refreshCharacterCallCount = 0

    override fun observeCharacters(): Flow<List<CharacterModel>> = observeCharactersFlow

    override suspend fun refreshCharacters(page: Int): CharactersRefreshResult {
        refreshCharactersCallCount++
        return refreshCharactersResult?.getOrThrow()
            ?: throw IllegalStateException("refreshCharactersResult is not set")
    }

    override fun observeCharacter(id: Int): Flow<CharacterModel?> = observeCharacterByIdFlow

    override suspend fun refreshCharacter(id: Int) {
        refreshCharacterCallCount++
        refreshCharacterResult?.getOrThrow()
    }
}
