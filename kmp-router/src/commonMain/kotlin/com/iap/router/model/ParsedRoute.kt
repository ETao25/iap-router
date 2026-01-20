package com.iap.router.model

data class ParsedRoute(
    val scheme: String,
    val path: String,
    val queryParams: Map<String, String> = emptyMap(),
    val pathParams: MutableMap<String, String> = mutableMapOf()
) {
    val pathSegments: List<String>
        get() = path.split("/").filter { it.isNotEmpty() }

    fun matchesPattern(pattern: String): Boolean {
        val patternSegments = pattern.split("/").filter { it.isNotEmpty() }
        val routeSegments = pathSegments

        if (patternSegments.isNotEmpty() && patternSegments.last() == "*") {
            val prefixSegments = patternSegments.dropLast(1)
            if (routeSegments.size < prefixSegments.size) return false
            return prefixSegments.zip(routeSegments).all { (p, r) ->
                p.startsWith(":") || p == r
            }
        }

        if (patternSegments.size != routeSegments.size) return false

        return patternSegments.zip(routeSegments).all { (p, r) ->
            p.startsWith(":") || p == r
        }
    }

    fun getAllParams(): Map<String, String> {
        return queryParams + pathParams
    }
}
