package com.rsav.githubPublicRepoBrowser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GitHubDarkColorScheme = darkColorScheme(
    primary = GhGreenDark,
    onPrimary = GhOnGreenDark,
    primaryContainer = GhGreenContainerDark,
    onPrimaryContainer = GhOnGreenContainerDark,
    secondary = GhBlueDark,
    onSecondary = GhOnBlueDark,
    secondaryContainer = GhBlueContainerDark,
    onSecondaryContainer = GhOnBlueContainerDark,
    tertiary = GhCoralDark,
    onTertiary = GhOnCoralDark,
    tertiaryContainer = GhCoralContainerDark,
    onTertiaryContainer = GhOnCoralContainerDark,
    error = GhCoralDark,
    onError = GhOnCoralDark,
    errorContainer = GhCoralContainerDark,
    onErrorContainer = GhOnCoralContainerDark,
    background = GhBgDark,
    onBackground = GhOnBgDark,
    surface = GhSurfaceDark,
    onSurface = GhOnSurfaceDark,
    surfaceVariant = GhSurfaceVariantDark,
    onSurfaceVariant = GhOnSurfaceVariantDark,
    outline = GhOutlineDark,
    outlineVariant = GhOutlineVariantDark,
)

private val GitHubLightColorScheme = lightColorScheme(
    primary = GhGreenLight,
    onPrimary = GhOnGreenLight,
    primaryContainer = GhGreenContainerLight,
    onPrimaryContainer = GhOnGreenContainerLight,
    secondary = GhBlueLight,
    onSecondary = GhOnBlueLight,
    secondaryContainer = GhBlueContainerLight,
    onSecondaryContainer = GhOnBlueContainerLight,
    tertiary = GhCoralLight,
    onTertiary = GhOnCoralLight,
    tertiaryContainer = GhCoralContainerLight,
    onTertiaryContainer = GhOnCoralContainerLight,
    error = GhCoralLight,
    onError = GhOnCoralLight,
    errorContainer = GhCoralContainerLight,
    onErrorContainer = GhOnCoralContainerLight,
    background = GhBgLight,
    onBackground = GhOnBgLight,
    surface = GhSurfaceLight,
    onSurface = GhOnSurfaceLight,
    surfaceVariant = GhSurfaceVariantLight,
    onSurfaceVariant = GhOnSurfaceVariantLight,
    outline = GhOutlineLight,
    outlineVariant = GhOutlineVariantLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) GitHubDarkColorScheme else GitHubLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
