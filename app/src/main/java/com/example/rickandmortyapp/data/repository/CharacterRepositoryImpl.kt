package com.example.rickandmortyapp.data.repository

import com.example.rickandmortyapp.data.local.dao.CharacterDao
import com.example.rickandmortyapp.data.mapper.toDomain
import com.example.rickandmortyapp.data.mapper.toEntity
import com.example.rickandmortyapp.data.remote.RickAndMortyApiService
import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharactersRefreshResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterRepositoryImpl(
    private val apiService: RickAndMortyApiService,
    private val characterDao: CharacterDao
) : CharacterRepository {

    override fun observeCharacters(): Flow<List<CharacterModel>> {
        return characterDao.observeCharacters().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun refreshCharacters(page: Int): CharactersRefreshResult {
        val response = apiService.getCharacters(page)

        val characters = response.results.map { dto ->
            dto.toEntity()
        }

        characterDao.upsertCharacters(characters)

        val nextPage = extractNextPage(response.info.next)

        return CharactersRefreshResult(
            nextPage = nextPage,
            isLastPage = nextPage == null
        )
    }

    override fun observeCharacter(id: Int): Flow<CharacterModel?> {
        return characterDao.observeCharacterById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun refreshCharacter(id: Int) {
        val dto = apiService.getCharacterById(id)
        val character = dto.toEntity()

        characterDao.upsertCharacter(character)
    }


}

private fun extractNextPage(nextUrl: String?): Int? {
    return nextUrl
        ?.substringAfter("page=", "")
        ?.substringBefore("&")
        ?.toIntOrNull()
}