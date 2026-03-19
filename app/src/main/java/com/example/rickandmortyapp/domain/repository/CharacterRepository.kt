package com.example.rickandmortyapp.domain.repository

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharacterPageResult

interface CharacterRepository {
    suspend fun getCharactersPage(page: Int): CharacterPageResult
    suspend fun getCharacterById(id: Int): CharacterModel
}