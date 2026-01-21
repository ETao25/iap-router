package com.iap.router.observer

import com.iap.router.model.RouteSource

/**
 * 路由结果类型
 */
enum class RouteResultType {
    /** 成功 */
    SUCCESS,

    /** 重定向 */
    REDIRECT,

    /** 被阻断 */
    BLOCKED,

    /** 错误 */
    ERROR,

    /** 路由未找到 */
    NOT_FOUND
}

/**
 * 路由事件（用于埋点）
 * 包含路由执行的完整信息
 */
data class RouteEvent(
    /**
     * 原始 URL
     */
    val url: String,

    /**
     * 路由来源
     */
    val source: RouteSource,

    /**
     * 路由结果类型
     */
    val resultType: RouteResultType,

    /**
     * 路由执行总耗时（毫秒）
     */
    val durationMs: Long,

    /**
     * 执行的拦截器链（按执行顺序）
     */
    val interceptorChain: List<String>,

    /**
     * 事件时间戳
     */
    val timestamp: Long,

    /**
     * 目标页面 ID（如果是页面路由）
     */
    val pageId: String? = null,

    /**
     * Action 名称（如果是 Action 路由）
     */
    val actionName: String? = null,

    /**
     * 错误信息（如果有）
     */
    val errorMessage: String? = null,

    /**
     * 重定向目标（如果是重定向）
     */
    val redirectUrl: String? = null,

    /**
     * 额外的自定义属性（用于业务扩展）
     */
    val extras: Map<String, Any?> = emptyMap()
) {
    companion object {
        /**
         * 创建成功事件
         */
        fun success(
            url: String,
            source: RouteSource,
            durationMs: Long,
            interceptorChain: List<String>,
            timestamp: Long,
            pageId: String? = null,
            actionName: String? = null
        ) = RouteEvent(
            url = url,
            source = source,
            resultType = RouteResultType.SUCCESS,
            durationMs = durationMs,
            interceptorChain = interceptorChain,
            timestamp = timestamp,
            pageId = pageId,
            actionName = actionName
        )

        /**
         * 创建重定向事件
         */
        fun redirect(
            url: String,
            source: RouteSource,
            durationMs: Long,
            interceptorChain: List<String>,
            timestamp: Long,
            redirectUrl: String
        ) = RouteEvent(
            url = url,
            source = source,
            resultType = RouteResultType.REDIRECT,
            durationMs = durationMs,
            interceptorChain = interceptorChain,
            timestamp = timestamp,
            redirectUrl = redirectUrl
        )

        /**
         * 创建阻断事件
         */
        fun blocked(
            url: String,
            source: RouteSource,
            durationMs: Long,
            interceptorChain: List<String>,
            timestamp: Long,
            reason: String
        ) = RouteEvent(
            url = url,
            source = source,
            resultType = RouteResultType.BLOCKED,
            durationMs = durationMs,
            interceptorChain = interceptorChain,
            timestamp = timestamp,
            errorMessage = reason
        )

        /**
         * 创建错误事件
         */
        fun error(
            url: String,
            source: RouteSource,
            durationMs: Long,
            interceptorChain: List<String>,
            timestamp: Long,
            errorMessage: String
        ) = RouteEvent(
            url = url,
            source = source,
            resultType = RouteResultType.ERROR,
            durationMs = durationMs,
            interceptorChain = interceptorChain,
            timestamp = timestamp,
            errorMessage = errorMessage
        )

        /**
         * 创建未找到事件
         */
        fun notFound(
            url: String,
            source: RouteSource,
            durationMs: Long,
            timestamp: Long
        ) = RouteEvent(
            url = url,
            source = source,
            resultType = RouteResultType.NOT_FOUND,
            durationMs = durationMs,
            interceptorChain = emptyList(),
            timestamp = timestamp
        )
    }
}
