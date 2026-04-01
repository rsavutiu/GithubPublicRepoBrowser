package com.rsav.githubPublicRepoBrowser.data.auth

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rsav.githubPublicRepoBrowser.BuildConfig
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "github_auth")

interface IGitHubAuthManager {
    val isLoggedIn: Flow<Boolean>
    val username: Flow<String?>
    val avatarUrl: Flow<String?>
    /** Emits after each successful login. Use to trigger pending actions. */
    val loginEvent: SharedFlow<Unit>
    suspend fun getAccessToken(): String?
    suspend fun getOAuthUrl(): String
    suspend fun exchangeCodeForToken(code: String, state: String): Boolean
    suspend fun logout()
}

@Singleton
class GitHubAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : IGitHubAuthManager {

    // Secure storage setup
    private val masterKeyAlias = MasterKeys.getOrCreate(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MasterKeys.AES256_GCM_SPEC
        } else {
            KeyGenParameterSpec.Builder(
                "_androidx_security_master_key_",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        }
    )

    private val securePrefs = EncryptedSharedPreferences.create(
        "secure_github_auth",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // HTTP client for OAuth operations (no cert pinning — GitHub rotates certs frequently)
    private val secureClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
        )
        .build()

    // Secure random for state generation
    private val secureRandom = SecureRandom()

    private val accessTokenKey = stringPreferencesKey("access_token")
    private val usernameKey = stringPreferencesKey("username")
    private val avatarKey = stringPreferencesKey("avatar_url")
    private val oauthStateKey = stringPreferencesKey("oauth_state")

    private val _loginEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val loginEvent: SharedFlow<Unit> = _loginEvent.asSharedFlow()

    override val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[accessTokenKey] != null
    }

    override val username: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[usernameKey]
    }

    override val avatarUrl: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[avatarKey]
    }

    override suspend fun getAccessToken(): String? =
        securePrefs.getString("access_token", null)

    override suspend fun getOAuthUrl(): String {
        val clientId = BuildConfig.GITHUB_CLIENT_ID
        val scopes = "public_repo" // needed for starring
        val redirectUri = "ghrepobrowser://oauth/callback"
        val state = generateSecureState()

        // Store state temporarily for CSRF protection
        try {
            withContext(Dispatchers.IO) {
                context.authDataStore.edit { prefs ->
                    prefs[oauthStateKey] = state
                }
            }
        } catch (e: Exception) {
            L.e(TAG, "Failed to store OAuth state: ${e.message}", e)
        }

        return "https://github.com/login/oauth/authorize?client_id=$clientId&scope=$scopes&redirect_uri=$redirectUri&state=$state"
    }

    private fun generateSecureState(): String {
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        } else {
            android.util.Base64.encodeToString(randomBytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
        }
    }

    override suspend fun exchangeCodeForToken(code: String, state: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Validate state parameter first (CSRF protection)
            val storedState = context.authDataStore.data.first()[oauthStateKey]
            if (storedState == null || storedState != state) {
                L.e(TAG, "Invalid state parameter - CSRF attempt detected")
                return@withContext false
            }

            // Clear state after validation
            context.authDataStore.edit { prefs ->
                prefs.remove(oauthStateKey)
            }

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

            val response = secureClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext false
            L.d(TAG, "Token exchange response: ${response.code}")

            val json = Json.parseToJsonElement(responseBody).jsonObject
            val token = json["access_token"]?.jsonPrimitive?.content ?: return@withContext false

            // Validate token with GitHub API
            if (!validateToken(token)) {
                L.e(TAG, "Token validation failed")
                return@withContext false
            }

            // Fetch user info
            val userInfo = fetchUserInfo(token)
            if (userInfo != null) {
                storeSecureToken(token, userInfo)
                L.i(TAG, "Secure login successful: ${userInfo.login}")
                _loginEvent.tryEmit(Unit)
                true
            } else {
                L.e(TAG, "Failed to fetch user info")
                false
            }
        } catch (e: Exception) {
            L.e(TAG, "Secure token exchange failed: ${e.message}", e)
            false
        }
    }

    private suspend fun validateToken(token: String): Boolean {
        return try {
            val request = Request.Builder()
                .url("https://api.github.com/user")
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = secureClient.newCall(request).execute()
            response.code == 200
        } catch (e: Exception) {
            L.e(TAG, "Token validation failed: ${e.message}", e)
            false
        }
    }

    private suspend fun fetchUserInfo(token: String): UserInfo? {
        return try {
            val request = Request.Builder()
                .url("https://api.github.com/user")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()

            val response = secureClient.newCall(request).execute()
            val userBody = response.body?.string()
            val userJson = userBody?.let { Json.parseToJsonElement(it).jsonObject }

            UserInfo(
                login = userJson?.get("login")?.jsonPrimitive?.content,
                avatar = userJson?.get("avatar_url")?.jsonPrimitive?.content
            )
        } catch (e: Exception) {
            L.e(TAG, "Failed to fetch user info: ${e.message}", e)
            null
        }
    }

    private suspend fun storeSecureToken(token: String, userInfo: UserInfo) {
        // Store sensitive data in encrypted SharedPreferences
        securePrefs.edit().apply {
            putString("access_token", token)
            putString("username", userInfo.login)
            putString("avatar_url", userInfo.avatar)
        }.apply()

        // Also store in DataStore for Flow compatibility (non-sensitive data only)
        withContext(Dispatchers.IO) {
            context.authDataStore.edit { prefs ->
                prefs[accessTokenKey] = "encrypted" // Indicator that token exists
                prefs[usernameKey] = userInfo.login ?: ""
                prefs[avatarKey] = userInfo.avatar ?: ""
            }
        }
    }

    data class UserInfo(
        val login: String?,
        val avatar: String?
    )

    override suspend fun logout() {
        try {
            // Clear encrypted storage
            securePrefs.edit().clear().apply()

            // Clear DataStore
            context.authDataStore.edit { it.clear() }

            L.i(TAG, "Secure logout completed")
        } catch (e: Exception) {
            L.e(TAG, "Error during logout: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "GitHubAuth"
    }
}
