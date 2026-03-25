package com.rsav.githubPublicRepoBrowser.domain.model

data class Dependency(val name: String, val version: String?)

data class DependencyInfo(
    val ecosystem: String,
    val sourceFile: String,
    val dependencies: List<Dependency>,
)
