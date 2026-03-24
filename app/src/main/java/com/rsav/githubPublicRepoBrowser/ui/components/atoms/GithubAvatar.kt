package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

@Composable
fun GithubAvatar(
    url: String,
    modifier: Modifier = Modifier,
    isOrganization: Boolean = false,
) {
    val shape = if (isOrganization) RoundedCornerShape(12.dp) else CircleShape
    val isLoading = remember { mutableStateOf(true) }

    Box(
        modifier = modifier.padding(8.dp),
    ) {
        if (isLoading.value) {
            ShimmerBox(modifier = Modifier.matchParentSize().clip(shape))
        }

        AsyncImage(
            modifier = Modifier.matchParentSize().clip(shape),
            model = url,
            contentDescription = null,
            onState = { state ->
                isLoading.value = state is AsyncImagePainter.State.Loading
            },
        )
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f),
    )

    Box(modifier = modifier.background(brush))
}
