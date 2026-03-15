package com.rsav.githubPublicRepoBrowser.di

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

    @Provides
    @Singleton
    fun provideParser(): Parser {
        return Parser.builder().build()
    }

    @Provides
    @Singleton
    fun provideHtmlRenderer(): HtmlRenderer {
        return HtmlRenderer.builder().build()
    }
}
