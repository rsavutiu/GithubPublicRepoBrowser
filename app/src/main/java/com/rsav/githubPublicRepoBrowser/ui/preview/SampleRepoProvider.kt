package com.rsav.githubPublicRepoBrowser.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

val sampleRepos = persistentListOf(
    Repo(
        id = "1",
        name = "compose-samples",
        nameWithOwner = "android/compose-samples",
        description = "Official Jetpack Compose samples demonstrating Material 3 components and architecture patterns.",
        url = "https://github.com/android/compose-samples",
        stargazerCount = 18_500,
        forkCount = 4_200,
        languageName = "Kotlin",
        languageColor = "#A97BFF",
        ownerLogin = "android",
        ownerAvatarUrl = "https://avatars.githubusercontent.com/u/32689599",
        ownerType = "Organization",
        createdAt = "2019-07-11T18:33:40Z",
        updatedAt = "2019-08-11T18:33:40Z",
        topics = listOf("android", "jetpack-compose", "material-design"),
    ),
    Repo(
        id = "2",
        name = "retrofit",
        nameWithOwner = "square/retrofit",
        description = "A type-safe HTTP client for Android and the JVM.",
        url = "https://github.com/square/retrofit",
        stargazerCount = 43_000,
        forkCount = 7_300,
        languageName = "Java",
        languageColor = "#B07219",
        ownerLogin = "square",
        ownerAvatarUrl = "https://avatars.githubusercontent.com/u/82592",
        createdAt = "2013-01-07T20:09:44Z",
        updatedAt = "2019-07-11T18:33:40Z",
        topics = listOf("http", "rest-api", "android"),
    ),
    Repo(
        id = "3",
        name = "dagger",
        nameWithOwner = "google/dagger",
        description = null,
        url = "https://github.com/google/dagger",
        stargazerCount = 17_400,
        forkCount = 2_000,
        languageName = null,
        languageColor = null,
        ownerLogin = "google",
        ownerAvatarUrl = "https://avatars.githubusercontent.com/u/1342004",
        ownerType = "Organization",
        createdAt = "2012-06-06T21:09:09Z",
        updatedAt = "2019-07-11T18:33:40Z",
    )
)

class SampleReposProvider: PreviewParameterProvider<ImmutableList<Repo>> {
    override val values: Sequence<ImmutableList<Repo>>
        get() = sequenceOf(sampleRepos)

}
class SampleRepoProvider : PreviewParameterProvider<Repo> {
    override val values: Sequence<Repo> = sampleRepos.asSequence()
}
