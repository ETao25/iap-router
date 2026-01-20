package com.iap.router.testutil

import com.iap.router.core.ProtocolParser
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteSource

/**
 * 创建测试用的 RouteContext
 */
fun createTestRouteContext(
    url: String = "iap://test/page",
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
        timestamp = 0L  // 测试环境使用固定值
    )
}
