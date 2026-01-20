package com.iap.router.interceptor

import com.iap.router.core.RouteMatcher
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult

/**
 * 局部拦截器注册信息
 *
 * @param pattern 路由模式，如 "payment/[*]" 或 "order/detail/:id"
 * @param interceptor 拦截器实例
 */
data class LocalInterceptorEntry(
    val pattern: String,
    val interceptor: RouteInterceptor
)

/**
 * 拦截器管理器
 *
 * 管理全局拦截器和局部拦截器的注册、移除和执行
 */
class InterceptorManager {

    private val globalInterceptors = mutableListOf<RouteInterceptor>()
    private val localInterceptors = mutableListOf<LocalInterceptorEntry>()

    /**
     * 添加全局拦截器
     *
     * 全局拦截器对所有路由生效
     */
    fun addGlobalInterceptor(interceptor: RouteInterceptor) {
        globalInterceptors.add(interceptor)
    }

    /**
     * 移除全局拦截器
     */
    fun removeGlobalInterceptor(interceptor: RouteInterceptor): Boolean {
        return globalInterceptors.remove(interceptor)
    }

    /**
     * 添加局部拦截器
     *
     * 局部拦截器仅对匹配 pattern 的路由生效
     *
     * @param pattern 路由模式，支持 path 参数和通配符
     * @param interceptor 拦截器实例
     */
    fun addInterceptor(pattern: String, interceptor: RouteInterceptor) {
        val normalizedPattern = normalizePattern(pattern)
        localInterceptors.add(LocalInterceptorEntry(normalizedPattern, interceptor))
    }

    /**
     * 移除指定 pattern 的局部拦截器
     */
    fun removeInterceptor(pattern: String, interceptor: RouteInterceptor): Boolean {
        val normalizedPattern = normalizePattern(pattern)
        return localInterceptors.removeAll {
            it.pattern == normalizedPattern && it.interceptor == interceptor
        }
    }

    /**
     * 移除指定 pattern 的所有拦截器
     */
    fun removeAllInterceptors(pattern: String): Int {
        val normalizedPattern = normalizePattern(pattern)
        val sizeBefore = localInterceptors.size
        localInterceptors.removeAll { it.pattern == normalizedPattern }
        return sizeBefore - localInterceptors.size
    }

    /**
     * 获取适用于当前路由的所有拦截器
     *
     * 包括所有全局拦截器 + 匹配 path 的局部拦截器
     *
     * @param path 路由路径
     * @return 合并后的拦截器列表（按优先级排序）
     */
    fun getApplicableInterceptors(path: String): List<RouteInterceptor> {
        val matchedLocalInterceptors = localInterceptors
            .filter { RouteMatcher.matches(path, it.pattern) }
            .map { it.interceptor }

        return (globalInterceptors + matchedLocalInterceptors)
            .sortedBy { it.priority }
    }

    /**
     * 执行拦截器链
     *
     * @param context 路由上下文
     * @param terminalHandler 终端处理器
     * @return 路由结果
     */
    suspend fun executeChain(
        context: RouteContext,
        terminalHandler: RouteTerminalHandler
    ): RouteResult {
        val interceptors = getApplicableInterceptors(context.parsedRoute.path)
        return executeInterceptorChain(interceptors, context, terminalHandler)
    }

    /**
     * 获取全局拦截器数量
     */
    fun globalInterceptorCount(): Int = globalInterceptors.size

    /**
     * 获取局部拦截器数量
     */
    fun localInterceptorCount(): Int = localInterceptors.size

    /**
     * 清空所有拦截器
     */
    fun clear() {
        globalInterceptors.clear()
        localInterceptors.clear()
    }

    /**
     * 获取所有注册的局部拦截器 pattern
     */
    fun getLocalPatterns(): List<String> {
        return localInterceptors.map { it.pattern }.distinct()
    }

    private fun normalizePattern(pattern: String): String {
        return pattern.trimStart('/')
    }
}
