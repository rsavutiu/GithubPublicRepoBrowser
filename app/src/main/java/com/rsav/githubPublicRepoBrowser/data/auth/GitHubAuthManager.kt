package com.rsav.githubPublicRepoBrowser.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rsav.githubPublicRepoBrowser.BuildConfig
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "github_auth")

@Singleton
class GitHubAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val usernameKey = stringPreferencesKey("username")
    private val avatarKey = stringPreferencesKey("avatar_url")

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[accessTokenKey] != null
    }

    val username: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[usernameKey]
    }

    val avatarUrl: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[avatarKey]
    }

    suspend fun getAccessToken(): String? =
        context.authDataStore.data.first()[accessTokenKey]

    fun getOAuthUrl(): String {
        val clientId = BuildConfig.GITHUB_CLIENT_ID
        val scopes = "public_repo" // needed for starring
        val redirectUri = "ghrepobrowser://oauth/callback"
        return "https://github.com/login/oauth/authorize?client_id=$clientId&scope=$scopes&redirect_uri=$redirectUri"
    }

    suspend fun exchangeCodeForToken(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val body = FormBody.Builder()
                .add("client_id", BuildConfig.GITHUB_CLIENT_ID)
                .add("client_secret", BuildConfig.GITHUB_CLIENT_SECRET)
                .add("code", code)
                .build()

            val request = Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .addHeader("Accept", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext false
            L.d(TAG, "Token exchange response: ${response.code}")

            val json = Json.parseToJsonElement(responseBody).jsonObject
            val token = json["access_token"]?.jsonPrimitive?.content ?: return@withContext false

            // Fetch user info
            val userRequest = Request.Builder()
                .url("https://api.github.com/user")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()

            val userResponse = client.newCall(userRequest).execute()
            val userBody = userResponse.body?.string()
            val userJson = userBody?.let { Json.parseToJsonElement(it).jsonObject }
            val login = userJson?.get("login")?.jsonPrimitive?.content
            val avatar = userJson?.get("avatar_url")?.jsonPrimitive?.content

            context.authDataStore.edit { prefs ->
                prefs[accessTokenKey] = token
                login?.let { prefs[usernameKey] = it }
                avatar?.let { prefs[avatarKey] = it }
            }

            L.i(TAG, "Login successful: $login")
            true
        } catch (e: Exception) {
            L.e(TAG, "Token exchange failed: ${e.message}", e)
            false
        }
    }

    suspend fun logout() {
        context.authDataStore.edit { it.clear() }
        L.i(TAG, "Logged out")
    }

    companion object {
        private const val TAG = "GitHubAuth"
    }
}
