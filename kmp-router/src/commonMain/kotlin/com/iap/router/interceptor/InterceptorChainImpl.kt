package com.iap.router.interceptor

import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult

/**
 * 路由终端处理器
 *
 * 当所有拦截器执行完毕后，由终端处理器执行实际的路由操作
 */
fun interface RouteTerminalHandler {
    /**
     * 执行实际的路由操作
     */
    suspend fun handle(context: RouteContext): RouteResult
}

/**
 * 拦截器链实现
 *
 * 按优先级顺序执行拦截器，最后调用终端处理器
 *
 * @param interceptors 排序后的拦截器列表（优先级从低到高排序，即 priority 值从小到大）
 * @param index 当前执行到的拦截器索引
 * @param context 当前路由上下文
 * @param terminalHandler 终端处理器，执行实际路由操作
 */
internal class InterceptorChainImpl(
    private val interceptors: List<RouteInterceptor>,
    private val index: Int,
    override val context: RouteContext,
    private val terminalHandler: RouteTerminalHandler
) : InterceptorChain {

    override suspend fun proceed(context: RouteContext): RouteResult {
        return if (index < interceptors.size) {
            // 还有拦截器需要执行
            val nextChain = InterceptorChainImpl(
                interceptors = interceptors,
                index = index + 1,
                context = context,
                terminalHandler = terminalHandler
            )
            interceptors[index].intercept(context, nextChain)
        } else {
            // 所有拦截器执行完毕，调用终端处理器
            terminalHandler.handle(context)
        }
    }
}

/**
 * 创建并启动拦截器链
 *
 * @param interceptors 拦截器列表（会自动按优先级排序）
 * @param context 初始路由上下文
 * @param terminalHandler 终端处理器
 * @return 路由结果
 */
suspend fun executeInterceptorChain(
    interceptors: List<RouteInterceptor>,
    context: RouteContext,
    terminalHandler: RouteTerminalHandler
): RouteResult {
    // 按优先级排序（数值越小优先级越高）
    val sortedInterceptors = interceptors.sortedBy { it.priority }

    val chain = InterceptorChainImpl(
        interceptors = sortedInterceptors,
        index = 0,
        context = context,
        terminalHandler = terminalHandler
    )

    return chain.proceed(context)
}
