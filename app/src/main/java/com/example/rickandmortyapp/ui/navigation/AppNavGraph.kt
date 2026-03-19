package com.example.rickandmortyapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rickandmortyapp.data.remote.RetrofitInstance
import com.example.rickandmortyapp.data.repository.CharacterRepositoryImpl
import com.example.rickandmortyapp.ui.screens.character_detail.CharacterDetailScreen
import com.example.rickandmortyapp.ui.screens.character_detail.CharacterDetailViewModel
import com.example.rickandmortyapp.ui.screens.character_detail.CharacterDetailViewModelFactory
import com.example.rickandmortyapp.ui.screens.character_list.CharacterListScreen
import com.example.rickandmortyapp.ui.screens.character_list.CharacterListViewModel
import com.example.rickandmortyapp.ui.screens.character_list.CharacterListViewModelFactory

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val repository = CharacterRepositoryImpl(RetrofitInstance.api)

    NavHost(
        navController = navController,
        startDestination = AppDestinations.CHARACTER_LIST_ROUTE
    ) {

        composable(AppDestinations.CHARACTER_LIST_ROUTE) {
            val listViewModel: CharacterListViewModel = viewModel(
                factory = CharacterListViewModelFactory(repository)
            )

            CharacterListScreen(
                viewModel = listViewModel,
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
                factory = CharacterDetailViewModelFactory(repository, characterId)
            )

            CharacterDetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}