package com.example.rickandmortyapp.ui.screens.character_list

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharactersRefreshResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import com.example.rickandmortyapp.domain.usecase.character.GetCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.LoadMoreCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.RefreshCharactersUseCase
import com.example.rickandmortyapp.fakes.FakeCharacterRepository
import com.example.rickandmortyapp.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleCharacters = listOf(
        CharacterModel(1, "Rick Sanchez", "Alive", "Human", "Male", "https://example.com/rick.png"),
    )

    @Test
    fun observeCharacters_whenSuccess_updatesUiStateWithCharacters() = runTest {
        val fakeRepository = FakeCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(fakeRepository),
            RefreshCharactersUseCase(fakeRepository),
            LoadMoreCharactersUseCase(fakeRepository),
        )

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(2, false))
        fakeRepository.observeCharactersFlow.emit(sampleCharacters)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(sampleCharacters, uiState.characters)
        assertFalse(uiState.isInitialLoading)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun observeCharacters_whenError_updatesUiStateWithError() = runTest {
        val fakeRepository = FakeCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(fakeRepository),
            RefreshCharactersUseCase(fakeRepository),
            LoadMoreCharactersUseCase(fakeRepository),
        )

        fakeRepository.refreshCharactersResult = Result.failure(RuntimeException("DB Error"))
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.characters.isEmpty())
        assertFalse(uiState.isInitialLoading)
        assertEquals("DB Error", uiState.errorMessage)
    }

    @Test
    fun loadMoreCharacters_whenNextPageExists_updatesStateCorrectly() = runTest {
        val fakeRepository = FakeCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(fakeRepository),
            RefreshCharactersUseCase(fakeRepository),
            LoadMoreCharactersUseCase(fakeRepository),
        )

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(2, false))
        fakeRepository.observeCharactersFlow.emit(sampleCharacters)
        advanceUntilIdle()

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(3, false))
        viewModel.loadMoreCharacters()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.endReached)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun loadMoreCharacters_whenRepositoryReturnsError_updatesErrorState() = runTest {
        val fakeRepository = FakeCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(fakeRepository),
            RefreshCharactersUseCase(fakeRepository),
            LoadMoreCharactersUseCase(fakeRepository),
        )

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(2, false))
        fakeRepository.observeCharactersFlow.emit(sampleCharacters)
        advanceUntilIdle()

        fakeRepository.refreshCharactersResult = Result.failure(RuntimeException("Network error"))
        viewModel.loadMoreCharacters()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingMore)
        assertEquals("Network error", uiState.errorMessage)
    }

    @Test
    fun loadMoreCharacters_whenNoNextPage_doesNotCallUseCaseAgain() = runTest {
        val fakeRepository = FakeCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(fakeRepository),
            RefreshCharactersUseCase(fakeRepository),
            LoadMoreCharactersUseCase(fakeRepository),
        )

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(2, false))
        fakeRepository.observeCharactersFlow.emit(sampleCharacters)
        advanceUntilIdle()

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(null, true))
        viewModel.loadMoreCharacters()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.endReached)

        viewModel.loadMoreCharacters()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.endReached)
    }

    @Test
    fun refreshCharacters_whenCalledAgainAfterError_updatesUiStateWithSuccess() = runTest {
        val fakeRepository = FakeCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(fakeRepository),
            RefreshCharactersUseCase(fakeRepository),
            LoadMoreCharactersUseCase(fakeRepository),
        )

        fakeRepository.refreshCharactersResult = Result.failure(RuntimeException("Network error"))
        advanceUntilIdle()

        viewModel.refreshCharacters()
        advanceUntilIdle()
        assertEquals("Network error", viewModel.uiState.value.errorMessage)

        fakeRepository.refreshCharactersResult = Result.success(CharactersRefreshResult(2, false))
        viewModel.refreshCharacters()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.endReached)
    }

    @Test
    fun refreshCharacters_whenRequestIsAlreadyInProgress_doesNotStartAnotherRequest() = runTest {
        val repository = BlockingCharacterRepository()
        val viewModel = CharacterListViewModel(
            GetCharactersUseCase(repository),
            RefreshCharactersUseCase(repository),
            LoadMoreCharactersUseCase(repository),
        )

        runCurrent()

        repository.refreshCharactersCallCount = 0

        viewModel.refreshCharacters()
        runCurrent()

        viewModel.refreshCharacters()
        runCurrent()

        assertEquals(1, repository.refreshCharactersCallCount)

        repository.release()

        repository.observeCharactersFlow.emit(sampleCharacters)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertEquals(1, uiState.characters.size)
        assertFalse(uiState.isInitialLoading)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.isRefreshing)
        assertNull(uiState.errorMessage)
    }

    private class BlockingCharacterRepository : CharacterRepository {
        var refreshCharactersCallCount = 0
        val observeCharactersFlow = MutableSharedFlow<List<CharacterModel>>(replay = 1)
        private val gate = kotlinx.coroutines.CompletableDeferred<Unit>()

        override fun observeCharacters(): Flow<List<CharacterModel>> = observeCharactersFlow

        override suspend fun refreshCharacters(page: Int): CharactersRefreshResult {
            refreshCharactersCallCount++
            gate.await()
            return CharactersRefreshResult(nextPage = 2, isLastPage = false)
        }

        override fun observeCharacter(id: Int): Flow<CharacterModel?> {
            throw UnsupportedOperationException("Not needed for this test")
        }

        override suspend fun refreshCharacter(id: Int) {
            throw UnsupportedOperationException("Not needed for this test")
        }

        fun release() {
            gate.complete(Unit)
        }
    }
}
