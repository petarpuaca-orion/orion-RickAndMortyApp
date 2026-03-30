package com.example.rickandmortyapp.domain.usecase.character

import com.example.rickandmortyapp.domain.model.CharacterDetailResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GetCharacterDetailUseCase(
    private val repository: CharacterRepository,
) {
    operator fun invoke(characterId: Int): Flow<CharacterDetailResult> =
        channelFlow {
            send(CharacterDetailResult.Loading)

            launch {
                repository.observeCharacter(characterId).collectLatest { character ->
                    send(CharacterDetailResult.Success(character))
                }
            }

            launch {
                try {
                    repository.refreshCharacter(characterId)
                } catch (e: Exception) {
                    send(CharacterDetailResult.Error(e.message ?: "Unknown error"))
                }
            }
        }
}
