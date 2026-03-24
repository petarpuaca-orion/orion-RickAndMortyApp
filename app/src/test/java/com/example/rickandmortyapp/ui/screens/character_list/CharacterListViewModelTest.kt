package com.example.rickandmortyapp.ui.screens.character_list

import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.domain.model.CharacterPageResult
import com.example.rickandmortyapp.domain.repository.CharacterRepository
import com.example.rickandmortyapp.fakes.FakeCharacterRepository
import com.example.rickandmortyapp.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun loadInitialCharacters_whenRepositoryReturnsSuccess_updatesUiStateWithCharacters() =
        runTest {
            val fakeRepository = FakeCharacterRepository()

            val expectedCharacters = listOf(
                CharacterModel(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    image = "https://example.com/rick.png"
                ),
                CharacterModel(
                    id = 2,
                    name = "Morty Smith",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    image = "https://example.com/morty.png"
                )
            )

            fakeRepository.charactersPageResult = Result.success(
                CharacterPageResult(
                    characters = expectedCharacters,
                    nextPage = 2,
                    isLastPage = false
                )
            )

            val viewModel = CharacterListViewModel(fakeRepository)

            advanceUntilIdle()

            val uiState = viewModel.uiState.value

            assertEquals(expectedCharacters, uiState.characters)
            assertFalse(uiState.isInitialLoading)
            assertFalse(uiState.isLoadingMore)
            assertFalse(uiState.isRefreshing)
            assertNull(uiState.errorMessage)
            assertFalse(uiState.endReached)
            assertEquals(1, fakeRepository.getCharactersPageCallCount)
        }

    @Test
    fun loadInitialCharacters_whenRepositoryReturnsError_updatesUiStateWithError() = runTest {
        val fakeRepository = FakeCharacterRepository()

        fakeRepository.charactersPageResult =
            Result.failure(RuntimeException("Network error"))

        val viewModel = CharacterListViewModel(fakeRepository)

        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertTrue(uiState.characters.isEmpty())
        assertFalse(uiState.isInitialLoading)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.isRefreshing)
        assertEquals("Failed to load characters.", uiState.errorMessage)
        assertFalse(uiState.endReached)
        assertEquals(1, fakeRepository.getCharactersPageCallCount)
    }

    @Test
    fun loadMoreCharacters_whenNextPageExists_appendsNextChunkToVisibleCharacters() = runTest {
        val fakeRepository = FakeCharacterRepository()

        val initialCharacters = listOf(
            CharacterModel(
                id = 1,
                name = "Rick Sanchez",
                status = "Alive",
                species = "Human",
                gender = "Male",
                image = "https://example.com/rick.png"
            )
        )

        val moreCharacters = listOf(
            CharacterModel(
                id = 2,
                name = "Morty Smith",
                status = "Alive",
                species = "Human",
                gender = "Male",
                image = "https://example.com/morty.png"
            ),
            CharacterModel(
                id = 3,
                name = "Summer Smith",
                status = "Alive",
                species = "Human",
                gender = "Female",
                image = "https://example.com/summer.png"
            )
        )

        fakeRepository.charactersPageResult = Result.success(
            CharacterPageResult(
                characters = initialCharacters,
                nextPage = 2,
                isLastPage = false
            )
        )

        val viewModel = CharacterListViewModel(fakeRepository)
        advanceUntilIdle()

        fakeRepository.charactersPageResult = Result.success(
            CharacterPageResult(
                characters = moreCharacters,
                nextPage = null,
                isLastPage = true
            )
        )

        viewModel.loadMoreCharacters()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertEquals(initialCharacters + moreCharacters, uiState.characters)
        assertFalse(uiState.isInitialLoading)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.isRefreshing)
        assertNull(uiState.errorMessage)
        assertTrue(uiState.endReached)
        assertEquals(2, fakeRepository.getCharactersPageCallCount)
    }

    @Test
    fun loadMoreCharacters_whenRepositoryReturnsError_keepsExistingCharactersAndUpdatesErrorState() =
        runTest {
            val fakeRepository = FakeCharacterRepository()

            val initialCharacters = listOf(
                CharacterModel(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    image = "https://example.com/rick.png"
                )
            )

            fakeRepository.charactersPageResult = Result.success(
                CharacterPageResult(
                    characters = initialCharacters,
                    nextPage = 2,
                    isLastPage = false
                )
            )

            val viewModel = CharacterListViewModel(fakeRepository)
            advanceUntilIdle()

            fakeRepository.charactersPageResult =
                Result.failure(RuntimeException("Failed to load more characters."))

            viewModel.loadMoreCharacters()
            advanceUntilIdle()

            val uiState = viewModel.uiState.value

            assertEquals(initialCharacters, uiState.characters)
            assertFalse(uiState.isInitialLoading)
            assertFalse(uiState.isLoadingMore)
            assertFalse(uiState.isRefreshing)
            assertEquals("Failed to load more characters.", uiState.errorMessage)
            assertFalse(uiState.endReached)
            assertEquals(2, fakeRepository.getCharactersPageCallCount)
        }

    @Test
    fun loadMoreCharacters_whenNoNextPage_doesNotCallRepositoryAgain() = runTest {
        val fakeRepository = FakeCharacterRepository()

        val initialCharacters = listOf(
            CharacterModel(
                id = 1,
                name = "Rick Sanchez",
                status = "Alive",
                species = "Human",
                gender = "Male",
                image = "https://example.com/rick.png"
            )
        )

        fakeRepository.charactersPageResult = Result.success(
            CharacterPageResult(
                characters = initialCharacters,
                nextPage = null,
                isLastPage = true
            )
        )

        val viewModel = CharacterListViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.loadMoreCharacters()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertEquals(initialCharacters, uiState.characters)
        assertFalse(uiState.isInitialLoading)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.isRefreshing)
        assertNull(uiState.errorMessage)
        assertTrue(uiState.endReached)
        assertEquals(1, fakeRepository.getCharactersPageCallCount)
    }

    @Test
    fun loadInitialCharacters_whenCalledAgainAfterError_updatesUiStateWithCharacters() = runTest {
        val fakeRepository = FakeCharacterRepository()

        fakeRepository.charactersPageResult =
            Result.failure(RuntimeException("Failed to load characters."))

        val viewModel = CharacterListViewModel(fakeRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.characters.isEmpty())
        assertEquals("Failed to load characters.", viewModel.uiState.value.errorMessage)

        val expectedCharacters = listOf(
            CharacterModel(
                id = 1,
                name = "Rick Sanchez",
                status = "Alive",
                species = "Human",
                gender = "Male",
                image = "https://example.com/rick.png"
            ),
            CharacterModel(
                id = 2,
                name = "Morty Smith",
                status = "Alive",
                species = "Human",
                gender = "Male",
                image = "https://example.com/morty.png"
            )
        )

        fakeRepository.charactersPageResult = Result.success(
            CharacterPageResult(
                characters = expectedCharacters,
                nextPage = 2,
                isLastPage = false
            )
        )

        viewModel.loadInitialCharacters()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertEquals(expectedCharacters, uiState.characters)
        assertFalse(uiState.isInitialLoading)
        assertFalse(uiState.isLoadingMore)
        assertFalse(uiState.isRefreshing)
        assertNull(uiState.errorMessage)
        assertFalse(uiState.endReached)
        assertEquals(2, fakeRepository.getCharactersPageCallCount)
    }

    @Test
    fun loadInitialCharacters_whenRequestIsAlreadyInProgress_doesNotStartAnotherRequest() =
        runTest {
            val repository = BlockingCharacterRepository()

            val viewModel = CharacterListViewModel(repository)

            runCurrent()

            viewModel.loadInitialCharacters()
            runCurrent()

            assertEquals(1, repository.getCharactersPageCallCount)

            repository.release()
            advanceUntilIdle()

            val uiState = viewModel.uiState.value

            assertEquals(1, uiState.characters.size)
            assertFalse(uiState.isInitialLoading)
            assertFalse(uiState.isLoadingMore)
            assertFalse(uiState.isRefreshing)
            assertNull(uiState.errorMessage)
        }
}

private class BlockingCharacterRepository : CharacterRepository {

    var getCharactersPageCallCount = 0

    private val gate = kotlinx.coroutines.CompletableDeferred<Unit>()

    override suspend fun getCharactersPage(page: Int): CharacterPageResult {
        getCharactersPageCallCount++

        gate.await()

        return CharacterPageResult(
            characters = listOf(
                CharacterModel(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    image = "https://example.com/rick.png"
                )
            ),
            nextPage = 2,
            isLastPage = false
        )
    }


    override suspend fun getCharacterById(id: Int): CharacterModel {
        throw UnsupportedOperationException("Not needed for this test")
    }

    fun release() {
        gate.complete(Unit)
    }
}