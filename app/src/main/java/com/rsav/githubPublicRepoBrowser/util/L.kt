package com.rsav.githubPublicRepoBrowser.util

import android.util.Log
import com.rsav.githubPublicRepoBrowser.BuildConfig

/**
 * Lightweight logger that compiles to no-ops in release builds.
 * Every call is guarded by BuildConfig.DEBUG which the compiler
 * can strip via dead-code elimination in release (R8/ProGuard).
 */
object L {
    private const val APP_TAG = "GHBrowser"

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d("$APP_TAG:$tag", msg)
    }

    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.i("$APP_TAG:$tag", msg)
    }

    fun w(tag: String, msg: String, t: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.w("$APP_TAG:$tag", msg, t)
    }

    fun e(tag: String, msg: String, t: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e("$APP_TAG:$tag", msg, t)
    }
}
