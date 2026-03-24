package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rsav.githubPublicRepoBrowser.util.L
import android.graphics.Color as AndroidColor

private const val TAG = "MarkdownWebView"

@Composable
fun MarkdownWebView(
    html: String,
    modifier: Modifier = Modifier,
) {
    L.d(TAG, "composing — html length=${html.length}")

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb().toHexColor()
    val bgColor = MaterialTheme.colorScheme.surface.toArgb().toHexColor()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb().toHexColor()
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant.toArgb().toHexColor()

    val styledHtml = remember(html, textColor, bgColor, linkColor, codeBgColor) {
        wrapWithTheme(html, textColor, bgColor, linkColor, codeBgColor)
    }

    val contentHeightDp = remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val heightModifier = if (contentHeightDp.intValue > 0) {
        modifier.then(Modifier.height(contentHeightDp.intValue.dp))
    } else {
        modifier.then(Modifier.height(200.dp)) // initial minimum while measuring
    }

    AndroidView(
        modifier = heightModifier,
        factory = { context ->
            L.d(TAG, "factory — creating WebView")
            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.defaultFontSize = 14
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false
                settings.javaScriptEnabled = true
                isVerticalScrollBarEnabled = false

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        // Measure content height and report back to Compose
                        view.evaluateJavascript("document.body.scrollHeight") { heightStr ->
                            val heightPx = heightStr.toIntOrNull() ?: return@evaluateJavascript
                            val dpValue = (heightPx / density.density).toInt()
                            if (dpValue > 0) {
                                contentHeightDp.intValue = dpValue
                            }
                        }
                    }
                }
            }
        },
        update = { webView ->
            L.d(TAG, "update — loading ${styledHtml.length} chars into WebView")
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        },
    )
}

private fun wrapWithTheme(
    html: String,
    textColor: String,
    bgColor: String,
    linkColor: String,
    codeBgColor: String,
): String = """
    <!DOCTYPE html>
    <html>
    <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            color: $textColor;
            background-color: $bgColor;
            font-family: sans-serif;
            font-size: 14px;
            line-height: 1.6;
            margin: 0;
            padding: 8px 0;
            word-wrap: break-word;
        }
        a { color: $linkColor; }
        code {
            background: $codeBgColor;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
        }
        pre {
            background: $codeBgColor;
            padding: 12px;
            border-radius: 8px;
            overflow-x: auto;
        }
        pre code { background: transparent; padding: 0; }
        img { max-width: 100%; height: auto; }
        h1, h2, h3 { margin-top: 16px; margin-bottom: 8px; }
        blockquote {
            border-left: 3px solid $linkColor;
            margin-left: 0;
            padding-left: 12px;
            opacity: 0.8;
        }
    </style>
    </head>
    <body>$html</body>
    </html>
""".trimIndent()

private fun Int.toHexColor(): String {
    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    return "#%02x%02x%02x".format(r, g, b)
}
