package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RepoRepositoryImplTest {

    private lateinit var dataSource: ApolloRepoDataSource
    private lateinit var repository: RepoRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = RepoRepositoryImpl(dataSource)
    }

    @Test
    fun `searchRepositories returns a non-null flow`() = runTest {
        val flow = repository.searchRepositories("kotlin")
        assertNotNull(flow)
    }
}
