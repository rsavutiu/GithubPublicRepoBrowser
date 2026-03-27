package com.rsav.githubPublicRepoBrowser.data.mapper

import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RepoMapperTest {

    @Test
    fun `maps all fields correctly`() {
        val onRepository = createOnRepository(
            id = "abc123",
            name = "my-repo",
            nameWithOwner = "user/my-repo",
            description = "A great repo",
            stargazerCount = 42,
            forkCount = 7,
            languageName = "Kotlin",
            languageColor = "#A97BFF",
            ownerLogin = "user",
        )

        val repo = onRepository.toDomainModel()

        assertEquals("abc123", repo.id)
        assertEquals("my-repo", repo.name)
        assertEquals("user/my-repo", repo.nameWithOwner)
        assertEquals("A great repo", repo.description)
        assertEquals("https://github.com/user/my-repo", repo.url)
        assertEquals(42, repo.stargazerCount)
        assertEquals(7, repo.forkCount)
        assertEquals("Kotlin", repo.languageName)
        assertEquals("#A97BFF", repo.languageColor)
        assertEquals("user", repo.ownerLogin)
        assertEquals("https://github.com/user.png", repo.ownerAvatarUrl)
    }

    @Test
    fun `null language maps to null fields`() {
        val onRepository = createOnRepository(
            id = "abc",
            name = "repo",
            nameWithOwner = "user/repo",
            description = null,
            stargazerCount = 0,
            forkCount = 0,
            languageName = null,
            languageColor = null,
            ownerLogin = "user",
        )

        val repo = onRepository.toDomainModel()

        assertNull(repo.languageName)
        assertNull(repo.languageColor)
        assertNull(repo.description)
    }

    @Test
    fun `url toString produces string representation`() {
        val onRepository = createOnRepository(
            id = "1",
            name = "repo",
            nameWithOwner = "user/repo",
            description = null,
            stargazerCount = 0,
            forkCount = 0,
            languageName = null,
            languageColor = null,
            ownerLogin = "user",
        )

        val repo = onRepository.toDomainModel()

        assertEquals("https://github.com/user/repo", repo.url)
        assertEquals("https://github.com/user.png", repo.ownerAvatarUrl)
    }

    private fun createOnRepository(
        id: String,
        name: String,
        nameWithOwner: String,
        description: String?,
        stargazerCount: Int,
        forkCount: Int,
        languageName: String?,
        languageColor: String?,
        ownerLogin: String,
    ): SearchRepositoriesQuery.OnRepository {
        return SearchRepositoriesQuery.OnRepository(
            id = id,
            name = name,
            nameWithOwner = nameWithOwner,
            description = description,
            url = "https://github.com/$nameWithOwner",
            stargazerCount = stargazerCount,
            forkCount = forkCount,
            primaryLanguage = if (languageName != null) {
                SearchRepositoriesQuery.PrimaryLanguage(
                    name = languageName,
                    color = languageColor,
                )
            } else {
                null
            },
            owner = SearchRepositoriesQuery.Owner(
                login = ownerLogin,
                avatarUrl = "https://github.com/$ownerLogin.png",
                __typename = "Organisation"
            ),
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-06-01T00:00:00Z",
            licenseInfo = null,
            openIssues = SearchRepositoriesQuery.OpenIssues(totalCount = 0),
            closedIssues = SearchRepositoriesQuery.ClosedIssues(totalCount = 0),
            repositoryTopics = SearchRepositoriesQuery.RepositoryTopics(nodes = emptyList()),
        )
    }
}
