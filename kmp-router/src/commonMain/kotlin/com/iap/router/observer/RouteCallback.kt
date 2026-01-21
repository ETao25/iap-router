package com.iap.router.observer

import com.iap.router.model.RouteContext

/**
 * 路由错误信息
 */
data class RouteError(
    /**
     * 错误码
     */
    val code: RouteErrorCode,

    /**
     * 错误消息
     */
    val message: String,

    /**
     * 原始 URL
     */
    val url: String,

    /**
     * 原始异常（可选）
     */
    val cause: Throwable? = null
)

/**
 * 路由错误码
 */
enum class RouteErrorCode {
    /** 路由未找到 */
    ROUTE_NOT_FOUND,

    /** 路由被拦截器阻断 */
    ROUTE_BLOCKED,

    /** 参数校验失败 */
    PARAM_VALIDATION_FAILED,

    /** 导航执行失败 */
    NAVIGATION_FAILED,

    /** 拦截器执行超时 */
    INTERCEPTOR_TIMEOUT,

    /** 未知错误 */
    UNKNOWN
}

/**
 * 路由结果回调
 * 用于单次路由调用的结果通知
 */
interface RouteCallback {
    /**
     * 路由成功时调用
     * @param context 路由上下文
     */
    fun onSuccess(context: RouteContext)

    /**
     * 路由失败时调用
     * @param error 错误信息
     */
    fun onError(error: RouteError)
}

/**
 * 简化的路由回调实现
 * 允许只实现需要的方法
 */
open class SimpleRouteCallback(
    private val onSuccessCallback: ((RouteContext) -> Unit)? = null,
    private val onErrorCallback: ((RouteError) -> Unit)? = null
) : RouteCallback {

    override fun onSuccess(context: RouteContext) {
        onSuccessCallback?.invoke(context)
    }

    override fun onError(error: RouteError) {
        onErrorCallback?.invoke(error)
    }
}
