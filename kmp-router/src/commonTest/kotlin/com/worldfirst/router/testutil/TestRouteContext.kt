package com.worldfirst.router.testutil

import com.worldfirst.router.core.ProtocolParser
import com.worldfirst.router.model.RouteContext
import com.worldfirst.router.model.RouteSource
import com.worldfirst.router.params.ObjectStore

/**
 * 创建测试用的 RouteContext
 */
fun createTestRouteContext(
    url: String = "worldfirst://test/page",
    params: Map<String, Any?> = emptyMap(),
    source: RouteSource = RouteSource.INTERNAL
): RouteContext {
    val parsedRoute = ProtocolParser.parseOrNull(url)
        ?: error("Invalid test url: $url")
    return RouteContext(
        url = url,
        parsedRoute = parsedRoute,
        params = params,
        source = source,
        timestamp = 0L,  // 测试环境使用固定值
        objectStore = ObjectStore()
    )
}
