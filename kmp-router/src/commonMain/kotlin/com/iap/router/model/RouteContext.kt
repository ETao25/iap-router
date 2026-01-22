package com.iap.router.model

/**
 * 路由上下文
 * 包含路由执行过程中的所有信息
 */
data class RouteContext(
    /** 原始 URL */
    val url: String,
    /** 解析后的路由信息 */
    val parsedRoute: ParsedRoute,
    /** 合并后的参数（URL path/query + params） */
    val params: Map<String, Any?>,
    /** 路由来源 */
    val source: RouteSource,
    /** 时间戳 */
    val timestamp: Long
) {
    /**
     * 创建带有额外参数的新 Context
     */
    fun withParams(extraParams: Map<String, Any?>): RouteContext {
        return copy(params = params + extraParams)
    }
}
