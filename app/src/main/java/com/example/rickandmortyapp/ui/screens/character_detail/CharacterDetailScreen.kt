package com.example.rickandmortyapp.ui.screens.character_detail


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.example.rickandmortyapp.domain.model.CharacterModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rickandmortyapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val character = uiState.character

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.character_details))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_content_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                DetailLoadingContent(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.errorMessage != null -> {
                DetailErrorContent(
                    message = uiState.errorMessage,
                    onRetry = { viewModel.loadCharacter() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            character != null -> {
                DetailContent(
                    character = character,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun DetailLoadingContent(modifier: Modifier= Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DetailErrorContent(
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
fun DetailContent(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AsyncImage(
            model = character.image,
            contentDescription = character.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Text(
            text = character.name,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.status_label, character.status),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(R.string.species_label, character.species),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(R.string.gender_label, character.gender),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailContentPreview() {
    MaterialTheme {
        DetailContent(
            character = CharacterModel(
                id = 4,
                name = "Beth Smith",
                status = "Alive",
                species = "Human",
                gender = "Female",
                image = "https://rickandmortyapi.com/api/character/avatar/4.jpeg"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailLoadingContentPreview() {
    MaterialTheme {
        DetailLoadingContent()
    }
}

@Preview(showBackground = true)
@Composable
fun DetailErrorContentPreview() {
    MaterialTheme {
        DetailErrorContent(
            message = "Failed to load character.",
            onRetry = {}
        )
    }
}