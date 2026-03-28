# ============================================================
# Project-specific ProGuard/R8 rules
# ============================================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- kotlinx.serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rsav.githubPublicRepoBrowser.**$$serializer { *; }
-keepclassmembers class com.rsav.githubPublicRepoBrowser.** {
    *** Companion;
}
-keepclasseswithmembers class com.rsav.githubPublicRepoBrowser.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- OkHttp / Okio ----
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Apollo GraphQL ----
-keep class com.apollographql.apollo.** { *; }
-keep class com.rsav.githubPublicRepoBrowser.type.** { *; }
-keep class com.rsav.githubPublicRepoBrowser.fragment.** { *; }
-keep class com.rsav.githubPublicRepoBrowser.*.adapter.** { *; }
-keep class com.rsav.githubPublicRepoBrowser.*.selections.** { *; }

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- Hilt ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ---- Compose ----
-dontwarn androidx.compose.**

# ---- Coil ----
-dontwarn coil3.**

# ---- CommonMark (Markdown) ----
-dontwarn org.commonmark.**
-keep class org.commonmark.** { *; }

# ---- Kotlin ----
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-keep class kotlin.Metadata { *; }

# ---- Cached JSON models (deserialized via kotlinx.serialization) ----
-keep class com.rsav.githubPublicRepoBrowser.data.remote.cached.** { *; }

# ---- Navigation serialization (type-safe nav args) ----
-keep class com.rsav.githubPublicRepoBrowser.domain.model.Repo { *; }
-keep class com.rsav.githubPublicRepoBrowser.ui.navigation.** { *; }
