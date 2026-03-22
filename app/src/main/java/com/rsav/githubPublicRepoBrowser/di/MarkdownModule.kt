package com.rsav.githubPublicRepoBrowser.di

import com.rsav.githubPublicRepoBrowser.util.L
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarkdownModule {

    private const val TAG = "MarkdownModule"

    @Provides
    @Singleton
    fun provideParser(): Parser {
        L.d(TAG, "Creating commonmark Parser")
        return Parser.builder().build()
    }

    @Provides
    @Singleton
    fun provideHtmlRenderer(): HtmlRenderer {
        L.d(TAG, "Creating commonmark HtmlRenderer")
        return HtmlRenderer.builder().build()
    }
}
