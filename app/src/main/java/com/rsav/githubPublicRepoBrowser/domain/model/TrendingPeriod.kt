package com.rsav.githubPublicRepoBrowser.domain.model

import java.time.LocalDate

enum class TrendingPeriod(val label: String, private val daysBack: Int) {
    TODAY("Today", 1),
    THIS_WEEK("This week", 7),
    THIS_MONTH("This month", 30);

    fun sinceDate(): String = LocalDate.now().minusDays(daysBack.toLong()).toString()
}
