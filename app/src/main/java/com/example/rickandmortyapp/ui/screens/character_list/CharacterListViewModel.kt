package com.example.rickandmortyapp.ui.screens.character_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterListViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterListUiState(isLoading = true))
    val uiState: StateFlow<CharacterListUiState> = _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    fun loadCharacters() {
        viewModelScope.launch {
            _uiState.value = CharacterListUiState(isLoading = true)
            try {
                val characters = repository.getCharacters()
                _uiState.value = CharacterListUiState(
                    isLoading = false,
                    characters = characters
                )
            } catch (e: Exception) {
                _uiState.value = CharacterListUiState(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}