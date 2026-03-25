package com.rsav.githubPublicRepoBrowser.domain.model

import androidx.annotation.StringRes
import com.rsav.githubPublicRepoBrowser.R
import java.time.LocalDate

enum class TrendingPeriod(@StringRes val labelRes: Int, private val daysBack: Int) {
    TODAY(R.string.trending_today, 1),
    THIS_WEEK(R.string.trending_this_week, 7),
    THIS_MONTH(R.string.trending_this_month, 30),
    THIS_YEAR(R.string.trending_this_year, 365);

    fun sinceDate(): String = LocalDate.now().minusDays(daysBack.toLong()).toString()
}
