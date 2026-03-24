package com.example.rickandmortyapp.ui.screens.character_detail

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.fakes.FakeCharacterRepository
import com.example.rickandmortyapp.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadCharacter_whenRepositoryReturnsSuccess_updatesUiStateWithCharacter() = runTest {
        val fakeRepository = FakeCharacterRepository()

        val expectedCharacter = CharacterModel(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            gender = "Male",
            image = "https://example.com/rick.png"
        )

        fakeRepository.characterByIdResult = Result.success(expectedCharacter)

        val viewModel = CharacterDetailViewModel(
            repository = fakeRepository,
            characterId = 1
        )

        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.isLoading)
        assertEquals(expectedCharacter, uiState.character)
        assertNull(uiState.errorMessage)
        assertEquals(1, fakeRepository.getCharacterByIdCallCount)
    }

    @Test
    fun loadCharacter_whenRepositoryReturnsError_updatesUiStateWithError() = runTest {
        val fakeRepository = FakeCharacterRepository()

        fakeRepository.characterByIdResult =
            Result.failure(RuntimeException("Network error"))

        val viewModel = CharacterDetailViewModel(
            repository = fakeRepository,
            characterId = 1
        )

        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.isLoading)
        assertNull(uiState.character)
        assertEquals("Network error", uiState.errorMessage)
        assertEquals(1, fakeRepository.getCharacterByIdCallCount)
    }

    @Test
    fun loadCharacter_whenCalledAgainAfterError_updatesUiStateWithSuccess() = runTest {
        val fakeRepository = FakeCharacterRepository()

        fakeRepository.characterByIdResult =
            Result.failure(RuntimeException("Network error"))

        val viewModel = CharacterDetailViewModel(
            repository = fakeRepository,
            characterId = 1
        )

        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.character)

        val expectedCharacter = CharacterModel(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            gender = "Male",
            image = "https://example.com/rick.png"
        )

        fakeRepository.characterByIdResult = Result.success(expectedCharacter)

        viewModel.loadCharacter()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertFalse(uiState.isLoading)
        assertEquals(expectedCharacter, uiState.character)
        assertNull(uiState.errorMessage)
        assertEquals(2, fakeRepository.getCharacterByIdCallCount)
    }
}