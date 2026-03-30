package com.example.rickandmortyapp.ui.screens.character_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.core.notifications.CharacterNotificationEvent
import com.example.rickandmortyapp.domain.model.CharacterListResult
import com.example.rickandmortyapp.domain.usecase.character.GetCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.LoadMoreCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.RefreshCharactersUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterListViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val refreshCharactersUseCase: RefreshCharactersUseCase,
    private val loadMoreCharactersUseCase: LoadMoreCharactersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CharacterListUiState(isInitialLoading = true))
    val uiState: StateFlow<CharacterListUiState> = _uiState.asStateFlow()

    private val _notificationEvent = MutableSharedFlow<CharacterNotificationEvent>()
    val notificationEvent: SharedFlow<CharacterNotificationEvent> =
        _notificationEvent.asSharedFlow()

    private var nextPageToLoad: Int? = 2
    private var lastPageReached = false
    private var requestInProgress = false

    private var shouldNotifyInitialLoadResult = true

    init {
        observeCharacters()
    }

    private suspend fun emitNotificationEvent(event: CharacterNotificationEvent) {
        _notificationEvent.emit(event)
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            getCharactersUseCase().collect { result ->
                when (result) {
                    CharacterListResult.Loading -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isInitialLoading = true,
                                errorMessage = null,
                            )
                        }
                    }

                    is CharacterListResult.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                characters = result.characters,
                                isInitialLoading = false,
                                errorMessage = null,
                            )
                        }

                        if (shouldNotifyInitialLoadResult) {
                            emitNotificationEvent(CharacterNotificationEvent.ShowSuccess)
                            shouldNotifyInitialLoadResult = false
                        }
                    }

                    is CharacterListResult.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isInitialLoading = false,
                                errorMessage = result.message,
                            )
                        }
                        if (shouldNotifyInitialLoadResult) {
                            emitNotificationEvent(CharacterNotificationEvent.ShowError)
                            shouldNotifyInitialLoadResult = false
                        }
                    }
                }
            }
        }
    }

    fun refreshCharacters() {
        if (requestInProgress) return

        viewModelScope.launch {
            requestInProgress = true

            try {
                _uiState.update { currentState ->
                    currentState.copy(
                        isRefreshing = true,
                        errorMessage = null,
                    )
                }

                runCatching {
                    refreshCharactersUseCase()
                }.onSuccess { result ->
                    nextPageToLoad = result.nextPage
                    lastPageReached = result.isLastPage

                    _uiState.update { currentState ->
                        currentState.copy(
                            isRefreshing = false,
                            endReached = result.isLastPage,
                        )
                    }
                    emitNotificationEvent(CharacterNotificationEvent.ShowSuccess)
                }.onFailure { throwable ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Unknown error",
                        )
                    }
                    emitNotificationEvent(CharacterNotificationEvent.ShowSuccess)
                }
            } finally {
                requestInProgress = false
            }
        }
    }

    fun loadMoreCharacters() {
        if (requestInProgress || lastPageReached || nextPageToLoad == null) return

        viewModelScope.launch {
            requestInProgress = true

            try {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoadingMore = true,
                        errorMessage = null,
                    )
                }

                val page = nextPageToLoad!!

                runCatching {
                    loadMoreCharactersUseCase(page)
                }.onSuccess { result ->
                    nextPageToLoad = result.nextPage
                    lastPageReached = result.isLastPage

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoadingMore = false,
                            endReached = result.isLastPage,
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoadingMore = false,
                            errorMessage = throwable.message ?: "Unknown error",
                        )
                    }
                }
            } finally {
                requestInProgress = false
            }
        }
    }
}
