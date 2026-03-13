package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun GithubAvatar(modifier: Modifier = Modifier, url: String, placeholder: Painter? = null) {
    AsyncImage(
        modifier = modifier.padding(8.dp),
        model = url,
        contentDescription = null,
        placeholder = placeholder
    )
}
