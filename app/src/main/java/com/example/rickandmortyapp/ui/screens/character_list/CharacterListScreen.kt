package com.example.rickandmortyapp.ui.screens.character_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rickandmortyapp.domain.model.CharacterModel
import com.example.rickandmortyapp.ui.components.CharacterCard
import androidx.compose.material3.Scaffold
import androidx.compose.ui.res.stringResource
import com.example.rickandmortyapp.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.rickandmortyapp.ui.theme.RickAndMortyAppTheme

@Composable
fun CharacterListScreen(
    viewModel: CharacterListViewModel,
    onCharacterClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingContent(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    message = uiState.errorMessage,
                    onRetry = { viewModel.loadCharacters() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                CharacterListContent(
                    characters = uiState.characters,
                    onCharacterClick = onCharacterClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun LoadingContent(modifier: Modifier= Modifier) {
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
    modifier: Modifier= Modifier,
    onRetry: () -> Unit
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

        Button(
            onClick = onRetry,
            modifier = modifier.padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
fun CharacterListContent(
    characters: List<CharacterModel>,
    modifier: Modifier= Modifier,
    onCharacterClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(characters) { character ->
            CharacterCard(
                character = character,
                onClick = { onCharacterClick(character.id) }
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun LoadingContentPreview() {
    RickAndMortyAppTheme {
        LoadingContent()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorContentPreview() {
    RickAndMortyAppTheme {
        ErrorContent(
            message = "Test error message",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterListContentPreview() {
    RickAndMortyAppTheme {
        CharacterListContent(
            characters = listOf(
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
                )
            ),
            onCharacterClick = {}
        )
    }
}