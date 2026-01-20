package com.iap.router.interceptor

import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult

/**
 * 路由拦截器接口
 *
 * 拦截器可以在路由执行前后进行处理，支持：
 * - 参数修改
 * - 路由重定向
 * - 路由阻断
 * - 异步拦截（通过 suspend function）
 */
interface RouteInterceptor {
    /**
     * 拦截器优先级，数值越小优先级越高
     * 默认优先级为 100
     */
    val priority: Int get() = 100

    /**
     * 拦截器名称，用于日志和调试
     */
    val name: String get() = this::class.simpleName ?: "Anonymous"

    /**
     * 执行拦截
     *
     * @param context 路由上下文，包含当前路由的所有信息
     * @param chain 拦截器链，调用 chain.proceed() 继续执行下一个拦截器
     * @return 路由结果
     */
    suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult
}

/**
 * 拦截器链接口
 *
 * 通过 proceed() 方法将控制权传递给下一个拦截器
 */
interface InterceptorChain {
    /**
     * 当前路由上下文
     */
    val context: RouteContext

    /**
     * 继续执行下一个拦截器
     *
     * @param context 可传入修改后的上下文，实现参数修改功能
     * @return 路由结果
     */
    suspend fun proceed(context: RouteContext = this.context): RouteResult
}
