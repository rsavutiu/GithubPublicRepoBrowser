package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun GithubAvatar(
    url: String,
    modifier: Modifier = Modifier,
    isOrganization: Boolean = false,
    placeholder: Painter? = null,
) {
    val shape = if (isOrganization) RoundedCornerShape(12.dp) else CircleShape
    AsyncImage(
        modifier = modifier
            .padding(8.dp)
            .clip(shape),
        model = url,
        contentDescription = null,
        placeholder = placeholder,
    )
}
