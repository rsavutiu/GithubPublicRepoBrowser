package com.rsav.githubPublicRepoBrowser.di

import android.net.TrafficStats
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import com.rsav.githubPublicRepoBrowser.BuildConfig
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.Socket
import javax.inject.Singleton
import javax.net.SocketFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TAG = "NetworkModule"
    private const val GITHUB_GRAPHQL_ENDPOINT = "https://api.github.com/graphql"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        L.d(TAG, "Creating OkHttpClient")
        return OkHttpClient.Builder()
            .socketFactory(TaggedSocketFactory())
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
                    .build()
                L.d(TAG, "→ ${request.method} ${request.url}")
                val response = chain.proceed(request)
                L.d(TAG, "← ${response.code} ${request.url} (${response.body?.contentLength() ?: "?"} bytes)")
                response
            })
            .build()
    }

    /** Tags all OkHttp sockets so StrictMode doesn't flag UntaggedSocketViolation. */
    private class TaggedSocketFactory : SocketFactory() {
        private val delegate = getDefault()

        override fun createSocket(): Socket =
            delegate.createSocket().also { TrafficStats.tagSocket(it) }

        override fun createSocket(host: String, port: Int): Socket =
            delegate.createSocket(host, port).also { TrafficStats.tagSocket(it) }

        override fun createSocket(host: String, port: Int, localHost: java.net.InetAddress, localPort: Int): Socket =
            delegate.createSocket(host, port, localHost, localPort).also { TrafficStats.tagSocket(it) }

        override fun createSocket(host: java.net.InetAddress, port: Int): Socket =
            delegate.createSocket(host, port).also { TrafficStats.tagSocket(it) }

        override fun createSocket(address: java.net.InetAddress, port: Int, localAddress: java.net.InetAddress, localPort: Int): Socket =
            delegate.createSocket(address, port, localAddress, localPort).also { TrafficStats.tagSocket(it) }
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        L.d(TAG, "Creating ApolloClient → $GITHUB_GRAPHQL_ENDPOINT")
        return ApolloClient.Builder()
            .serverUrl(GITHUB_GRAPHQL_ENDPOINT)
            .okHttpClient(okHttpClient)
            .build()
    }
}
