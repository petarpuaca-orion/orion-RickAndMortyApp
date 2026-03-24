package com.example.rickandmortyapp.ui.screens.character_list

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rickandmortyapp.R
import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.ui.components.CharacterCard

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    viewModel: CharacterListViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onCharacterClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    CharacterListPaginationEffect(
        listState = listState,
        uiState = uiState,
        onLoadMore = { viewModel.loadMoreCharacters() }
    )

    Scaffold { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                Log.d("UI", "onRefreshTriggered")
                viewModel.refreshCharacters()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CharacterListScreenContent(
                uiState = uiState,
                listState = listState,
                onRetry = { viewModel.loadInitialCharacters() },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onCharacterClick = onCharacterClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CharacterListScreenContent(
    uiState: CharacterListUiState,
    listState: LazyListState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRetry: () -> Unit,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isInitialLoading && uiState.characters.isEmpty() -> {
            LoadingContent(modifier = modifier)
        }

        uiState.errorMessage != null && uiState.characters.isEmpty() -> {
            ErrorContent(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = modifier
            )
        }

        else -> {
            CharacterListContent(
                characters = uiState.characters,
                listState = listState,
                isLoadingMore = uiState.isLoadingMore,
                onCharacterClick = onCharacterClick,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = modifier
            )
        }
    }
}

@Composable
fun CharacterListPaginationEffect(
    listState: LazyListState,
    uiState: CharacterListUiState,
    onLoadMore: () -> Unit
) {
    LaunchedEffect(listState, uiState.characters.size, uiState.endReached) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 3
        }.collect { shouldLoadMore ->
            if (
                shouldLoadMore &&
                !uiState.isInitialLoading &&
                !uiState.isRefreshing &&
                !uiState.isLoadingMore &&
                !uiState.endReached
            ) {
                onLoadMore()
            }
        }
    }
}

@Composable
fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message ?: stringResource(R.string.something_went_wrong),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun CharacterListContent(
    characters: List<CharacterModel>,
    listState: LazyListState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isLoadingMore: Boolean,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = characters,
            key = { character -> character.id }
        ) { character ->
            CharacterCard(
                character = character,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onCharacterClick(character.id) }
            )
        }

        if (isLoadingMore) {
            item {
                LoadingMoreItem()
            }
        }
    }
}

@Composable
fun LoadingMoreItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun previewCharacters() = listOf(
    CharacterModel(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
    ),
    CharacterModel(
        id = 2,
        name = "Morty Smith",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://rickandmortyapi.com/api/character/avatar/2.jpeg"
    ),
    CharacterModel(
        id = 3,
        name = "Summer Smith",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://rickandmortyapi.com/api/character/avatar/3.jpeg"
    )
)

@Preview(showBackground = true)
@Composable
fun LoadingContentPreview() {
    LoadingContent()
}

@Preview(showBackground = true)
@Composable
fun ErrorContentPreview() {
    ErrorContent(
        message = "Failed to load characters.",
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ErrorContentWithDefaultMessagePreview() {
    ErrorContent(
        message = null,
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoadingMoreItemPreview() {
    LoadingMoreItem()
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun CharacterListContentPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            CharacterListContent(
                characters = previewCharacters(),
                listState = rememberLazyListState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
                isLoadingMore = false,
                onCharacterClick = {}
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun CharacterListContentLoadingMorePreview() {
    SharedTransitionLayout {

        AnimatedVisibility(visible = true) {
            CharacterListContent(
                characters = previewCharacters(),
                listState = rememberLazyListState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
                isLoadingMore = false,
                onCharacterClick = {}
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun CharacterListScreenContentLoadingPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            CharacterListScreenContent(
                uiState = CharacterListUiState(
                    isInitialLoading = true,
                    characters = emptyList()
                ),
                listState = rememberLazyListState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
                onRetry = {},
                onCharacterClick = {}
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun CharacterListScreenContentErrorPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            CharacterListScreenContent(
                uiState = CharacterListUiState(
                    isInitialLoading = false,
                    characters = emptyList(),
                    errorMessage = "Something went wrong."
                ),
                listState = rememberLazyListState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
                onRetry = {},
                onCharacterClick = {}
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun CharacterListScreenContentSuccessPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            CharacterListScreenContent(
                uiState = CharacterListUiState(
                    isInitialLoading = false,
                    characters = previewCharacters(),
                    isLoadingMore = false,
                    endReached = false,
                    errorMessage = null
                ),
                listState = rememberLazyListState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
                onRetry = {},
                onCharacterClick = {}
            )
        }
    }
}