package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri

data class AiProvider(
    val name: String,
    val packageName: String,
    val webUrl: String,
    val brandColor: Color,
    val icon: ImageVector,
    val tagline: String,
)

val AI_PROVIDERS = listOf(
    AiProvider(
        name = "Claude",
        packageName = "com.anthropic.claude",
        webUrl = "https://claude.ai/new",
        brandColor = Color(0xFFD97757),
        icon = Icons.Default.AutoAwesome,
        tagline = "By Anthropic",
    ),
    AiProvider(
        name = "ChatGPT",
        packageName = "com.openai.chatgpt",
        webUrl = "https://chatgpt.com",
        brandColor = Color(0xFF10A37F),
        icon = Icons.Default.SmartToy,
        tagline = "By OpenAI",
    ),
    AiProvider(
        name = "Gemini",
        packageName = "com.google.android.apps.bard",
        webUrl = "https://gemini.google.com",
        brandColor = Color(0xFF4285F4),
        icon = Icons.Default.Diamond,
        tagline = "By Google",
    ),
    AiProvider(
        name = "Copilot",
        packageName = "com.microsoft.copilot",
        webUrl = "https://copilot.microsoft.com",
        brandColor = Color(0xFF6264A7),
        icon = Icons.Default.Code,
        tagline = "By Microsoft",
    ),
    AiProvider(
        name = "Meta AI",
        packageName = "com.facebook.orca",
        webUrl = "https://www.meta.ai",
        brandColor = Color(0xFF0668E1),
        icon = Icons.Default.Bolt,
        tagline = "By Meta",
    ),
    AiProvider(
        name = "Perplexity",
        packageName = "ai.perplexity.app.android",
        webUrl = "https://www.perplexity.ai",
        brandColor = Color(0xFF20B8CD),
        icon = Icons.Default.Search,
        tagline = "Answer engine",
    ),
    AiProvider(
        name = "DeepSeek",
        packageName = "com.deepseek.chat",
        webUrl = "https://chat.deepseek.com",
        brandColor = Color(0xFF4D6BFE),
        icon = Icons.Default.Psychology,
        tagline = "By DeepSeek",
    ),
)

fun getInstalledAiProviders(context: Context): List<AiProvider> {
    val pm = context.packageManager
    return AI_PROVIDERS.filter { provider ->
        try {
            pm.getPackageInfo(provider.packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}

fun launchAiProvider(context: Context, provider: AiProvider, prompt: String) {
    // Try app share intent first
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, prompt)
        setPackage(provider.packageName)
    }
    try {
        context.startActivity(shareIntent)
        return
    } catch (_: Exception) {
        // App not available for share, fall through to web
    }

    // Fallback: copy to clipboard and open web URL
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("AI prompt", prompt))

    val browserIntent = Intent(Intent.ACTION_VIEW, provider.webUrl.toUri())
    try {
        context.startActivity(browserIntent)
        Toast.makeText(context, "Prompt copied! Paste it in ${provider.name}.", Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
        Toast.makeText(context, "Prompt copied to clipboard.", Toast.LENGTH_SHORT).show()
    }
}
