package com.rsav.githubPublicRepoBrowser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rsav.githubPublicRepoBrowser.data.auth.GitHubAuthManager
import com.rsav.githubPublicRepoBrowser.ui.navigation.AppNavGraph
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: GitHubAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L.i(TAG, "onCreate — savedInstanceState=${savedInstanceState != null}")
        enableEdgeToEdge()
        handleOAuthCallback(intent)

        setContent {
            MyApplicationTheme {
                AppNavGraph()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "ghrepobrowser" && uri.host == "oauth") {
            val code = uri.getQueryParameter("code") ?: return
            L.i(TAG, "OAuth callback received with code")
            MainScope().launch {
                authManager.exchangeCodeForToken(code)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        L.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        L.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        L.d(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
