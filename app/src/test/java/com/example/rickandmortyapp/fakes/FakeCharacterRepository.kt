package com.example.rickandmortyapp.fakes

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharacterPageResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository

class FakeCharacterRepository : CharacterRepository {

    var characterByIdResult: Result<CharacterModel>? = null
    var charactersPageResult: Result<CharacterPageResult>? = null

    var getCharacterByIdCallCount = 0
    var getCharactersPageCallCount = 0

    override suspend fun getCharacterById(id: Int): CharacterModel {
        getCharacterByIdCallCount++

        return characterByIdResult?.getOrThrow()
            ?: throw IllegalStateException("characterByIdResult is not set")
    }

    override suspend fun getCharactersPage(page: Int): CharacterPageResult {
        getCharactersPageCallCount++

        return charactersPageResult?.getOrThrow()
            ?: throw IllegalStateException("charactersPageResult is not set")
    }
}