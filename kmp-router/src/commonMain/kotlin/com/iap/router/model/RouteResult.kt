package com.iap.router.model

/**
 * 路由执行结果
 * 使用密封类确保类型安全
 */
sealed class RouteResult {
    /**
     * 路由执行成功
     */
    data class Success(val context: RouteContext) : RouteResult()

    /**
     * 重定向到其他路由
     */
    data class Redirect(
        val newUrl: String,
        val newParams: Map<String, Any?> = emptyMap()
    ) : RouteResult()

    /**
     * 路由被拦截/阻断
     */
    data class Blocked(val reason: String) : RouteResult()

    /**
     * 路由执行出错
     */
    data class Error(val exception: Throwable) : RouteResult()

    /**
     * 路由未找到
     */
    data class NotFound(val url: String) : RouteResult()
}
