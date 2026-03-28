package com.example.rickandmortyapp.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.rickandmortyapp.data.local.database.AppDatabase
import com.example.rickandmortyapp.data.remote.RetrofitInstance
import com.example.rickandmortyapp.data.repository.CharacterRepositoryImpl
import com.example.rickandmortyapp.domain.usecase.character.GetCharacterDetailUseCase
import com.example.rickandmortyapp.domain.usecase.character.GetCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.LoadMoreCharactersUseCase
import com.example.rickandmortyapp.domain.usecase.character.RefreshCharactersUseCase
import com.example.rickandmortyapp.ui.screens.character_detail.CharacterDetailScreen
import com.example.rickandmortyapp.ui.screens.character_detail.CharacterDetailViewModel
import com.example.rickandmortyapp.ui.screens.character_detail.CharacterDetailViewModelFactory
import com.example.rickandmortyapp.ui.screens.character_list.CharacterListScreen
import com.example.rickandmortyapp.ui.screens.character_list.CharacterListViewModel
import com.example.rickandmortyapp.ui.screens.character_list.CharacterListViewModelFactory

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "rick_and_morty_database"
        ).build()
    }

    val repository = remember {
        CharacterRepositoryImpl(
            apiService = RetrofitInstance.api,
            characterDao = database.characterDao()
        )
    }

    val getCharactersUseCase = remember { GetCharactersUseCase(repository) }
    val refreshCharactersUseCase = remember { RefreshCharactersUseCase(repository) }
    val loadMoreCharactersUseCase = remember { LoadMoreCharactersUseCase(repository) }
    val getCharacterDetailUseCase = remember { GetCharacterDetailUseCase(repository) }

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.CHARACTER_LIST_ROUTE
        ) {

            composable(AppDestinations.CHARACTER_LIST_ROUTE) {
                val listViewModel: CharacterListViewModel = viewModel(
                    factory = CharacterListViewModelFactory(
                        getCharactersUseCase = getCharactersUseCase,
                        refreshCharactersUseCase = refreshCharactersUseCase,
                        loadMoreCharactersUseCase = loadMoreCharactersUseCase
                    )
                )

                CharacterListScreen(
                    viewModel = listViewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onCharacterClick = { characterId ->
                        navController.navigate(
                            "${AppDestinations.CHARACTER_DETAIL_ROUTE}/$characterId"
                        )
                    }
                )
            }

            composable(
                route = "${AppDestinations.CHARACTER_DETAIL_ROUTE}/{${AppDestinations.CHARACTER_ID_ARG}}",
                arguments = listOf(
                    navArgument(AppDestinations.CHARACTER_ID_ARG) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val characterId =
                    backStackEntry.arguments?.getInt(AppDestinations.CHARACTER_ID_ARG) ?: 0

                val detailViewModel: CharacterDetailViewModel = viewModel(
                    factory = CharacterDetailViewModelFactory(
                        getCharacterDetailUseCase = getCharacterDetailUseCase,
                        characterId = characterId
                    )
                )

                CharacterDetailScreen(
                    viewModel = detailViewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}