package com.iap.router.core

import com.iap.router.model.ParsedRoute

data class RouteMatchResult(
    val pattern: String,
    val pathParams: Map<String, String>,
    val score: Int
)

object RouteMatcher {

    fun match(path: String, pattern: String): RouteMatchResult? {
        val pathSegments = path.split("/").filter { it.isNotEmpty() }
        val patternSegments = pattern.split("/").filter { it.isNotEmpty() }

        if (patternSegments.isNotEmpty() && patternSegments.last() == "*") {
            return matchWildcard(pathSegments, patternSegments)
        }

        if (pathSegments.size != patternSegments.size) {
            return null
        }

        val pathParams = mutableMapOf<String, String>()
        var score = 0

        for ((pathSeg, patternSeg) in pathSegments.zip(patternSegments)) {
            when {
                patternSeg.startsWith(":") -> {
                    val paramName = patternSeg.substring(1)
                    pathParams[paramName] = pathSeg
                    score += 1
                }
                patternSeg == pathSeg -> {
                    score += 10
                }
                else -> {
                    return null
                }
            }
        }

        return RouteMatchResult(
            pattern = pattern,
            pathParams = pathParams,
            score = score
        )
    }

    private fun matchWildcard(
        pathSegments: List<String>,
        patternSegments: List<String>
    ): RouteMatchResult? {
        val prefixSegments = patternSegments.dropLast(1)

        if (pathSegments.size < prefixSegments.size) {
            return null
        }

        val pathParams = mutableMapOf<String, String>()
        var score = 0

        for ((pathSeg, patternSeg) in pathSegments.take(prefixSegments.size).zip(prefixSegments)) {
            when {
                patternSeg.startsWith(":") -> {
                    val paramName = patternSeg.substring(1)
                    pathParams[paramName] = pathSeg
                    score += 1
                }
                patternSeg == pathSeg -> {
                    score += 10
                }
                else -> {
                    return null
                }
            }
        }

        val wildcardPart = pathSegments.drop(prefixSegments.size).joinToString("/")
        if (wildcardPart.isNotEmpty()) {
            pathParams["*"] = wildcardPart
        }

        return RouteMatchResult(
            pattern = patternSegments.joinToString("/"),
            pathParams = pathParams,
            score = score
        )
    }

    fun findBestMatch(path: String, patterns: Collection<String>): RouteMatchResult? {
        return patterns
            .mapNotNull { pattern -> match(path, pattern) }
            .maxByOrNull { it.score }
    }

    fun matches(path: String, pattern: String): Boolean {
        return match(path, pattern) != null
    }

    fun fillPathParams(parsedRoute: ParsedRoute, matchResult: RouteMatchResult): ParsedRoute {
        parsedRoute.pathParams.putAll(matchResult.pathParams)
        return parsedRoute
    }

    fun hasPathParams(pattern: String): Boolean {
        return pattern.contains(":")
    }

    fun hasWildcard(pattern: String): Boolean {
        return pattern.endsWith("/*") || pattern.endsWith("*")
    }

    fun extractParamNames(pattern: String): List<String> {
        return pattern.split("/")
            .filter { it.startsWith(":") }
            .map { it.substring(1) }
    }
}
