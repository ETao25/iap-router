package com.iap.router.observer

import com.iap.router.interceptor.RouteInterceptor
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult

/**
 * 路由事件观察者
 * 用于监听路由生命周期事件（埋点、监控等）
 */
interface RouteObserver {
    /**
     * 路由开始时调用
     * @param context 路由上下文
     */
    fun onRouteStart(context: RouteContext)

    /**
     * 路由完成时调用（无论成功或失败）
     * @param context 路由上下文
     * @param result 路由结果
     */
    fun onRouteComplete(context: RouteContext, result: RouteResult)

    /**
     * 拦截器执行完成时调用
     * @param interceptor 执行的拦截器
     * @param context 路由上下文
     * @param durationMs 执行耗时（毫秒）
     */
    fun onInterceptorExecuted(interceptor: RouteInterceptor, context: RouteContext, durationMs: Long)
}

/**
 * 简化的路由观察者实现
 * 允许只实现需要的方法
 */
open class SimpleRouteObserver(
    private val onStart: ((RouteContext) -> Unit)? = null,
    private val onComplete: ((RouteContext, RouteResult) -> Unit)? = null,
    private val onInterceptor: ((RouteInterceptor, RouteContext, Long) -> Unit)? = null
) : RouteObserver {

    override fun onRouteStart(context: RouteContext) {
        onStart?.invoke(context)
    }

    override fun onRouteComplete(context: RouteContext, result: RouteResult) {
        onComplete?.invoke(context, result)
    }

    override fun onInterceptorExecuted(interceptor: RouteInterceptor, context: RouteContext, durationMs: Long) {
        onInterceptor?.invoke(interceptor, context, durationMs)
    }
}
