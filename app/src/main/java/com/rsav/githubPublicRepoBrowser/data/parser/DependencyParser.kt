package com.rsav.githubPublicRepoBrowser.data.parser

import com.rsav.githubPublicRepoBrowser.domain.model.Dependency
import org.json.JSONObject

object DependencyParser {

    fun parsePackageJson(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        try {
            val json = JSONObject(content)
            extractJsonDeps(json, "dependencies", deps)
            extractJsonDeps(json, "devDependencies", deps)
        } catch (_: Exception) {
            // malformed JSON — return empty
        }
        return deps
    }

    fun parseRequirementsTxt(content: String): List<Dependency> {
        return content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") && !it.startsWith("-") }
            .mapNotNull { line ->
                val separators = listOf("==", ">=", "<=", "~=", "!=", ">", "<")
                var name = line
                var version: String? = null
                for (sep in separators) {
                    val idx = line.indexOf(sep)
                    if (idx > 0) {
                        name = line.substring(0, idx).trim()
                        version = line.substring(idx + sep.length).trim()
                        break
                    }
                }
                // strip extras like package[extra]
                val bracketIdx = name.indexOf('[')
                if (bracketIdx > 0) name = name.substring(0, bracketIdx)
                if (name.isNotBlank()) Dependency(name, version) else null
            }
    }

    fun parseBuildGradle(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        // Match patterns like: implementation("group:artifact:version"), api("group:artifact"), kapt("..."), ksp("...")
        // Also handle single-quoted Groovy: implementation 'group:artifact:version'
        val regex = Regex(
            """(?:implementation|api|kapt|ksp|compileOnly|runtimeOnly|testImplementation|androidTestImplementation)\s*[\("']\s*([^"')]+)\s*["')]"""
        )
        for (match in regex.findAll(content)) {
            val coordinate = match.groupValues[1].trim()
            if (coordinate.startsWith("libs.") || coordinate.startsWith("project(")) continue
            val parts = coordinate.split(":")
            when {
                parts.size >= 3 -> deps.add(Dependency("${parts[0]}:${parts[1]}", parts[2]))
                parts.size == 2 -> deps.add(Dependency("${parts[0]}:${parts[1]}", null))
                else -> if (coordinate.isNotBlank()) deps.add(Dependency(coordinate, null))
            }
        }
        // Also capture libs.xxx version catalog references
        val catalogRegex = Regex(
            """(?:implementation|api|kapt|ksp|compileOnly|runtimeOnly|testImplementation|androidTestImplementation)\s*\(\s*(libs\.[a-zA-Z0-9._]+)\s*\)"""
        )
        for (match in catalogRegex.findAll(content)) {
            val ref = match.groupValues[1].trim()
            deps.add(Dependency(ref, null))
        }
        return deps
    }

    fun parseGoMod(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        var inRequireBlock = false
        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("require (") || trimmed == "require (") {
                inRequireBlock = true
                continue
            }
            if (inRequireBlock && trimmed == ")") {
                inRequireBlock = false
                continue
            }
            if (inRequireBlock) {
                val parts = trimmed.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    deps.add(Dependency(parts[0], parts[1]))
                }
            }
            // Single-line require
            if (trimmed.startsWith("require ") && !trimmed.contains("(")) {
                val parts = trimmed.removePrefix("require ").trim().split(Regex("\\s+"))
                if (parts.size >= 2) {
                    deps.add(Dependency(parts[0], parts[1]))
                }
            }
        }
        return deps
    }

    fun parseCargoToml(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        var inDepsSection = false
        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.matches(Regex("""\[(.*dependencies.*)]"""))) {
                inDepsSection = true
                continue
            }
            if (trimmed.startsWith("[") && !trimmed.contains("dependencies")) {
                inDepsSection = false
                continue
            }
            if (inDepsSection && trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                val eqIndex = trimmed.indexOf('=')
                if (eqIndex > 0) {
                    val name = trimmed.substring(0, eqIndex).trim()
                    val value = trimmed.substring(eqIndex + 1).trim()
                    val version = when {
                        value.startsWith("\"") -> value.trim('"')
                        value.startsWith("{") -> {
                            val versionMatch = Regex("""version\s*=\s*"([^"]+)"""").find(value)
                            versionMatch?.groupValues?.get(1)
                        }
                        else -> null
                    }
                    deps.add(Dependency(name, version))
                }
            }
        }
        return deps
    }

    fun parsePubspecYaml(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        var inDepsSection = false
        for (line in content.lines()) {
            // Top-level key detection
            if (!line.startsWith(" ") && !line.startsWith("\t") && line.trimEnd().endsWith(":")) {
                val key = line.trimEnd().removeSuffix(":")
                inDepsSection = key == "dependencies" || key == "dev_dependencies"
                continue
            }
            if (inDepsSection) {
                // Lines at indentation level 1 are dependency entries
                if (line.startsWith("  ") && !line.startsWith("    ")) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("#")) continue
                    val colonIdx = trimmed.indexOf(':')
                    if (colonIdx > 0) {
                        val name = trimmed.substring(0, colonIdx).trim()
                        val value = trimmed.substring(colonIdx + 1).trim()
                        val version = when {
                            value.startsWith("^") || value.startsWith("~") || value.first().isDigit() -> value
                            value.isBlank() -> null // nested block follows
                            else -> value
                        }
                        if (name.isNotBlank()) deps.add(Dependency(name, version))
                    }
                }
            }
        }
        return deps
    }

    fun parsePomXml(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        val regex = Regex(
            """<dependency>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>(?:\s*<version>([^<]+)</version>)?""",
            RegexOption.DOT_MATCHES_ALL,
        )
        for (match in regex.findAll(content)) {
            val groupId = match.groupValues[1].trim()
            val artifactId = match.groupValues[2].trim()
            val version = match.groupValues.getOrNull(3)?.trim()?.takeIf { it.isNotBlank() }
            deps.add(Dependency("$groupId:$artifactId", version))
        }
        return deps
    }

    fun parseComposerJson(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        try {
            val json = JSONObject(content)
            extractJsonDeps(json, "require", deps)
            extractJsonDeps(json, "require-dev", deps)
        } catch (_: Exception) {
            // malformed JSON
        }
        return deps
    }

    fun parseGemfile(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        val regex = Regex("""gem\s+['"]([^'"]+)['"](?:\s*,\s*['"]([^'"]+)['"])?""")
        for (match in regex.findAll(content)) {
            val name = match.groupValues[1]
            val version = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
            deps.add(Dependency(name, version))
        }
        return deps
    }

    fun parsePodfile(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        val regex = Regex("""pod\s+['"]([^'"]+)['"](?:\s*,\s*['"]([^'"]+)['"])?""")
        for (match in regex.findAll(content)) {
            val name = match.groupValues[1]
            val version = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
            deps.add(Dependency(name, version))
        }
        return deps
    }

    fun parsePackageSwift(content: String): List<Dependency> {
        val deps = mutableListOf<Dependency>()
        // Match .package(url: "https://github.com/user/repo.git", from: "1.0.0")
        // Match .package(url: "https://github.com/user/repo", .upToNextMajor(from: "1.0.0"))
        // Match .package(url: "https://github.com/user/repo.git", exact: "1.0.0")
        // Match .package(url: "https://github.com/user/repo.git", branch: "main")
        val regex = Regex(
            """\.\s*package\s*\(\s*(?:name\s*:\s*"[^"]*"\s*,\s*)?url\s*:\s*"([^"]+)"(?:\s*,\s*(?:from\s*:\s*"([^"]+)"|exact\s*:\s*"([^"]+)"|branch\s*:\s*"([^"]+)"|\.\s*upToNextMajor\s*\(\s*from\s*:\s*"([^"]+)"\s*\)|\.\s*upToNextMinor\s*\(\s*from\s*:\s*"([^"]+)"\s*\)|"([^"]+)"\s*\.\.\.\s*"([^"]+)"))?"""
        )
        for (match in regex.findAll(content)) {
            val url = match.groupValues[1]
            // Extract repo name from URL: https://github.com/user/repo.git -> user/repo
            val name = url
                .removePrefix("https://github.com/")
                .removePrefix("http://github.com/")
                .removeSuffix(".git")
                .ifBlank { url }
            val version = match.groupValues[2].takeIf { it.isNotBlank() }
                ?: match.groupValues[3].takeIf { it.isNotBlank() }
                ?: match.groupValues[4].takeIf { it.isNotBlank() }?.let { "branch:$it" }
                ?: match.groupValues[5].takeIf { it.isNotBlank() }
                ?: match.groupValues[6].takeIf { it.isNotBlank() }
                ?: match.groupValues[7].takeIf { it.isNotBlank() }?.let { "${it}...${match.groupValues[8]}" }
            deps.add(Dependency(name, version))
        }
        return deps
    }

    private fun extractJsonDeps(json: JSONObject, key: String, out: MutableList<Dependency>) {
        if (!json.has(key)) return
        val obj = json.optJSONObject(key) ?: return
        for (name in obj.keys()) {
            val version = obj.optString(name).takeIf { it.isNotBlank() }
            out.add(Dependency(name, version))
        }
    }
}
