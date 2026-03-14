package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import android.graphics.Color as AndroidColor
import android.webkit.WebView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MarkdownWebView(
    html: String,
    modifier: Modifier = Modifier,
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb().toHexColor()
    val bgColor = MaterialTheme.colorScheme.surface.toArgb().toHexColor()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb().toHexColor()
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant.toArgb().toHexColor()

    val styledHtml = remember(html, textColor, bgColor, linkColor, codeBgColor) {
        wrapWithTheme(html, textColor, bgColor, linkColor, codeBgColor)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.defaultFontSize = 14
                isVerticalScrollBarEnabled = false
            }
        },
        update = { webView ->
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
