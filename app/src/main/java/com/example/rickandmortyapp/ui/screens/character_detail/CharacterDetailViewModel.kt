package com.example.rickandmortyapp.ui.screens.character_detail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.domain.model.CharacterDetailResult
import com.example.rickandmortyapp.domain.usecase.character.GetCharacterDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase,
    private val characterId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterDetailUiState(isLoading = true))
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()

    init {
        loadCharacterDetail()
    }

    fun loadCharacterDetail() {
        viewModelScope.launch {
            getCharacterDetailUseCase(characterId).collect { result ->
                when (result) {
                    CharacterDetailResult.Loading -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                    }

                    is CharacterDetailResult.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                character = result.character,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }

                    is CharacterDetailResult.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }
}