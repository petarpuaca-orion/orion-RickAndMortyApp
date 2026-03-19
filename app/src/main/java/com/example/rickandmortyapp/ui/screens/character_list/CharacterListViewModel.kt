package com.example.rickandmortyapp.ui.screens.character_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterListViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterListUiState(isInitialLoading = true))
    val uiState: StateFlow<CharacterListUiState> = _uiState.asStateFlow()

    private val visibleCharacters = mutableListOf<CharacterModel>()
    private val bufferedCharacters = mutableListOf<CharacterModel>()

    private var nextPage: Int? = 1
    private var lastPageReached = false
    private var requestInProgress = false

    init {
        loadInitialCharacters()
    }

    fun loadInitialCharacters() {
        if (requestInProgress) return

        viewModelScope.launch {
            requestInProgress = true

            _uiState.update {
                it.copy(
                    isInitialLoading = true,
                    isLoadingMore = false,
                    errorMessage = null
                )
            }

            try {
                visibleCharacters.clear()
                bufferedCharacters.clear()
                nextPage = 1
                lastPageReached = false

                val result = repository.getCharactersPage(page = 1)

                visibleCharacters.addAll(result.characters)
                nextPage = result.nextPage
                lastPageReached = result.isLastPage

                publishState()
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isLoadingMore = false,
                        errorMessage = "Failed to load characters."
                    )
                }
            } finally {
                requestInProgress = false
            }
        }
    }

    fun loadMoreCharacters() {
        if (requestInProgress) return
        if (lastPageReached && bufferedCharacters.isEmpty()) return

        viewModelScope.launch {
            requestInProgress = true

            _uiState.update {
                it.copy(
                    isLoadingMore = true,
                    errorMessage = null
                )
            }

            try {
                appendNextChunk()

                publishState()
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = "Failed to load more characters."
                    )
                }
            } finally {
                requestInProgress = false
            }
        }
    }

    private suspend fun appendNextChunk() {
        if (bufferedCharacters.size < 10 && !lastPageReached) {
            val pageToLoad = nextPage ?: return

            val result = repository.getCharactersPage(pageToLoad)
            bufferedCharacters.addAll(result.characters)

            nextPage = result.nextPage
            lastPageReached = result.isLastPage
        }

        val chunkSize = minOf(10, bufferedCharacters.size)
        if (chunkSize > 0) {
            val nextChunk = bufferedCharacters.take(chunkSize)
            bufferedCharacters.subList(0, chunkSize).clear()
            visibleCharacters.addAll(nextChunk)
        }
    }

    private fun publishState() {
        _uiState.value = CharacterListUiState(
            characters = visibleCharacters.toList(),
            isInitialLoading = false,
            isLoadingMore = false,
            errorMessage = null,
            endReached = lastPageReached && bufferedCharacters.isEmpty()
        )
    }
}