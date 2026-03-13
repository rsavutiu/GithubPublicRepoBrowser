package com.rsav.githubPublicRepoBrowser.ui.navigation

import android.content.Intent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.screen.detail.RepoDetailScreen
import com.rsav.githubPublicRepoBrowser.ui.screen.search.RepoSearchScreen
import kotlinx.serialization.json.Json

//Maybe a bit too slow/long
private const val FADE_DURATION_MS = 1000

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = SearchRoute,
            modifier = modifier,
        ) {
            composable<SearchRoute>(
                exitTransition = { fadeOut(tween(FADE_DURATION_MS)) },
                popEnterTransition = { fadeIn(tween(FADE_DURATION_MS)) },
            ) {
                RepoSearchScreen(
                    onRepoClick = { repo ->
                        val json = Json.encodeToString(Repo.serializer(), repo)
                        navController.navigate(DetailRoute(repoJson = json))
                    },
                    modifier = modifier,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }

            composable<DetailRoute>(
                enterTransition = { fadeIn(tween(FADE_DURATION_MS)) },
                popExitTransition = { fadeOut(tween(FADE_DURATION_MS)) },
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<DetailRoute>()
                val repo = Json.decodeFromString(Repo.serializer(), route.repoJson)

                RepoDetailScreen(
                    repo = repo,
                    onBack = { navController.navigateUp() },
                    onOpenUrl = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                    },
                    modifier = modifier,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }
        }
    }
}
