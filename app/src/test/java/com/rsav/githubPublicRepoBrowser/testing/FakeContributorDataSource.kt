package com.rsav.githubPublicRepoBrowser.testing

/**
 * Fake contributor data source that returns pre-configured counts.
 */
class FakeContributorDataSource {

    private val counts = mutableMapOf<String, Int>()

    fun setCount(owner: String, repo: String, count: Int) {
        counts["$owner/$repo"] = count
    }

    suspend fun getContributorCount(owner: String, repo: String): Int? {
        return counts["$owner/$repo"]
    }
}
