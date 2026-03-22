package com.rsav.githubPublicRepoBrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rsav.githubPublicRepoBrowser.ui.navigation.AppNavGraph
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L.i(TAG, "onCreate — savedInstanceState=${savedInstanceState != null}")
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AppNavGraph()
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
