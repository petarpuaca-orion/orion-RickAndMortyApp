package com.example.rickandmortyapp.data.repository

import com.example.rickandmortyapp.data.mapper.toDomain
import com.example.rickandmortyapp.data.remote.RickAndMortyApiService
import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharacterPageResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository

class CharacterRepositoryImpl(
    private val apiService: RickAndMortyApiService
) : CharacterRepository {

    private val cachedPages = mutableMapOf<Int, CharacterPageResult>()
    private val cachedCharactersById = mutableMapOf<Int, CharacterModel>()

    override suspend fun getCharactersPage(page: Int): CharacterPageResult {
        cachedPages[page]?.let { cachedPage ->
            return cachedPage
        }

        val response = apiService.getCharacters(page)

        val characters = response.results.map { dto ->
            dto.toDomain()
        }

        characters.forEach { character ->
            cachedCharactersById[character.id] = character
        }

        val result = CharacterPageResult(
            characters = characters,
            nextPage = extractNextPage(response.info.next),
            isLastPage = response.info.next == null
        )

        cachedPages[page] = result
        return result
    }

    override suspend fun getCharacterById(id: Int): CharacterModel {
        cachedCharactersById[id]?.let { cachedCharacter ->
            return cachedCharacter
        }

        val dto = apiService.getCharacterById(id)

        val character = dto.toDomain()

        cachedCharactersById[id] = character
        return character
    }

    private fun extractNextPage(nextUrl: String?): Int? {
        return nextUrl
            ?.substringAfter("page=", "")
            ?.substringBefore("&")
            ?.toIntOrNull()
    }
}