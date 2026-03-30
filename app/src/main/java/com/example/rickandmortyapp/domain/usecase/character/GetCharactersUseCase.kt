package com.example.rickandmortyapp.domain.usecase.character

import com.example.rickandmortyapp.domain.model.CharacterListResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GetCharactersUseCase(
    private val repository: CharacterRepository,
) {
    operator fun invoke(): Flow<CharacterListResult> =
        channelFlow {
            send(CharacterListResult.Loading)

            launch {
                repository.observeCharacters().collectLatest { characters ->
                    send(
                        CharacterListResult.Success(
                            characters = characters,
                            nextPage = null,
                            isLastPage = false,
                        ),
                    )
                }
            }

            launch {
                try {
                    repository.refreshCharacters(page = 1)
                } catch (e: Exception) {
                    send(CharacterListResult.Error(e.message ?: "Unknown error"))
                }
            }
        }
}
