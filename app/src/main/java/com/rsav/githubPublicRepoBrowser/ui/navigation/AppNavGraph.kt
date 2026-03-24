package com.rsav.githubPublicRepoBrowser.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.screen.detail.RepoDetailScreen
import com.rsav.githubPublicRepoBrowser.ui.screen.search.RepoSearchScreen
import com.rsav.githubPublicRepoBrowser.ui.screen.userrepos.UserReposScreen
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.serialization.json.Json

//Maybe a bit too slow/long
private const val FADE_DURATION_MS = 1000
private const val TAG = "NavGraph"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = SearchRoute,
            modifier = modifier,
        ) {
            composable<SearchRoute>(
                exitTransition = { fadeOut(tween(FADE_DURATION_MS)) },
                popEnterTransition = { fadeIn(tween(FADE_DURATION_MS)) },
            ) { backStackEntry ->
                L.d(TAG, "composable → SearchRoute")

                // Handle topic selected from detail screen
                val savedStateHandle = backStackEntry.savedStateHandle
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.rsav.githubPublicRepoBrowser.ui.screen.search.RepoSearchViewModel>(backStackEntry)
                val selectedTopic = savedStateHandle.get<String>("selected_topic")
                LaunchedEffect(selectedTopic) {
                    selectedTopic?.let {
                        viewModel.onIntent(com.rsav.githubPublicRepoBrowser.ui.screen.search.SearchIntent.TopicSelected(it))
                        savedStateHandle.remove<String>("selected_topic")
                    }
                }

                RepoSearchScreen(
                    onNavigateToDetail = { repo ->
                        L.d(TAG, "navigating to detail: ${repo.nameWithOwner}")
                        val json = Json.encodeToString<Repo>(repo)
                        navController.navigate(DetailRoute(repoJson = json))
                    },
                    modifier = modifier,
                    viewModel = viewModel,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }

            composable<DetailRoute>(
                enterTransition = { fadeIn(tween(FADE_DURATION_MS)) },
                popExitTransition = { fadeOut(tween(FADE_DURATION_MS)) },
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<DetailRoute>()
                val repo = Json.decodeFromString<Repo>(route.repoJson)
                L.d(TAG, "composable → DetailRoute for ${repo.nameWithOwner} \n readme: ${repo.readmeText}")

                RepoDetailScreen(
                    repo = repo,
                    onNavigateBack = {
                        L.d(TAG, "navigateUp from detail")
                        navController.navigateUp()
                    },
                    onTopicClick = { topic ->
                        L.d(TAG, "topic clicked: $topic")
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_topic", topic)
                        navController.navigateUp()
                    },
                    onOwnerClick = { ownerLogin ->
                        L.d(TAG, "owner clicked: $ownerLogin")
                        navController.navigate(UserReposRoute(userLogin = ownerLogin))
                    },
                    modifier = modifier,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }

            composable<UserReposRoute>(
                enterTransition = { fadeIn(tween(FADE_DURATION_MS)) },
                popExitTransition = { fadeOut(tween(FADE_DURATION_MS)) },
            ) {
                UserReposScreen(
                    onNavigateToDetail = { repo ->
                        L.d(TAG, "navigating to detail from user repos: ${repo.nameWithOwner}")
                        val json = Json.encodeToString<Repo>(repo)
                        navController.navigate(DetailRoute(repoJson = json))
                    },
                    onNavigateBack = {
                        L.d(TAG, "navigateUp from user repos")
                        navController.navigateUp()
                    },
                    modifier = modifier,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }
        }
    }
}
