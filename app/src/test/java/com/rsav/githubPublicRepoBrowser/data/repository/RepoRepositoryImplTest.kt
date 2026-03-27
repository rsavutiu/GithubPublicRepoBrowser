package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.testing.FakeApolloRepoDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RepoRepositoryImplTest {

    private lateinit var fakeDataSource: FakeApolloRepoDataSource
    private lateinit var repository: SearchRepositoriesImpl

    @Before
    fun setUp() {
        fakeDataSource = FakeApolloRepoDataSource()
        repository = SearchRepositoriesImpl(fakeDataSource::searchRepositories)
    }

    @Test
    fun `searchRepositories returns a non-null flow`() = runTest {
        val flow = repository.searchRepositories("kotlin")
        assertNotNull(flow)
    }
}
