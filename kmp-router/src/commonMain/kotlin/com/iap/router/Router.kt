package com.iap.router

import com.iap.router.core.ActionCallback
import com.iap.router.core.ProtocolParser
import com.iap.router.core.RouteLookupResult
import com.iap.router.core.RouteTable
import com.iap.router.fallback.FallbackAction
import com.iap.router.fallback.FallbackHandler
import com.iap.router.fallback.DefaultFallbackHandler
import com.iap.router.interceptor.InterceptorManager
import com.iap.router.interceptor.RouteInterceptor
import com.iap.router.interceptor.RouteTerminalHandler
import com.iap.router.model.NavMode
import com.iap.router.model.NavigationOptions
import com.iap.router.model.ParsedRoute
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.model.RouteSource
import com.iap.router.observer.ObserverManager
import com.iap.router.observer.RouteCallback
import com.iap.router.observer.RouteError
import com.iap.router.observer.RouteErrorCode
import com.iap.router.observer.RouteObserver
import com.iap.router.platform.ActionExecutor
import com.iap.router.platform.Navigator
import com.iap.router.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 路由器
 *
 * 核心入口类，负责：
 * - URL 解析
 * - 拦截器链执行
 * - 路由查找
 * - 页面导航 / Action 执行
 * - 降级处理
 * - 观察者通知
 *
 * 使用单例模式，通过 Router.shared 访问
 *
 * 初始化示例（iOS）：
 * ```swift
 * Router.shared.initialize()
 * ```
 *
 * 初始化示例（Android）：
 * ```kotlin
 * Router.shared.initialize()
 * ```
 *
 * 注意：通常应使用 Router.shared 单例，不建议直接创建新实例
 */
class Router(
    /**
     * 路由表
     */
    val routeTable: RouteTable = RouteTable(),

    /**
     * 拦截器管理器
     */
    val interceptorManager: InterceptorManager = InterceptorManager(),

    /**
     * 观察者管理器
     */
    val observerManager: ObserverManager = ObserverManager(),

    /**
     * 协程上下文（用于异步拦截器执行）
     */
    coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
) {
    companion object {
        /**
         * 单例实例
         */
        val shared: Router = Router()
    }

    /**
     * 是否已初始化
     */
    private var initialized = false

    /**
     * 路由注册器
     */
    val registry: RouteRegistry = RouteRegistry(routeTable)

    /**
     * 导航器（由平台初始化方法设置）
     */
    internal var navigator: Navigator? = null

    /**
     * Action 执行器（由平台初始化方法设置）
     */
    internal var actionExecutor: ActionExecutor? = null

    /**
     * 降级处理器
     */
    var fallbackHandler: FallbackHandler = DefaultFallbackHandler()

    /**
     * 协程作用域
     */
    private val scope = CoroutineScope(coroutineContext)

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean = initialized

    /**
     * 标记为已初始化（由平台实现调用）
     */
    internal fun markInitialized() {
        initialized = true
    }

    // ==================== 路由打开 API ====================

    /**
     * 打开路由
     *
     * @param url 路由 URL，如 "iap://order/detail/123?from=list"
     * @param params 额外参数（会合并到 URL 参数中）
     * @param options 导航选项
     * @param source 路由来源
     * @param callback 结果回调
     */
    fun open(
        url: String,
        params: Map<String, Any?> = emptyMap(),
        options: NavigationOptions = NavigationOptions(),
        source: RouteSource = RouteSource.INTERNAL,
        callback: RouteCallback? = null
    ) {
        scope.launch {
            val result = openSuspend(url, params, options, source)
            handleResult(result, url, callback)
        }
    }

    /**
     * 挂起函数版本的路由打开
     */
    suspend fun openSuspend(
        url: String,
        params: Map<String, Any?> = emptyMap(),
        options: NavigationOptions = NavigationOptions(),
        source: RouteSource = RouteSource.INTERNAL
    ): RouteResult {
        // 1. 解析 URL
        val parsedRoute = ProtocolParser.parseOrNull(url)
        if (parsedRoute == null) {
            Logger.error("Failed to parse URL: $url")
            return RouteResult.Error(IllegalArgumentException("Invalid URL: $url"))
        }

        // 2. 构建初始上下文
        val initialParams = buildMergedParams(parsedRoute, params)
        val context = RouteContext(
            url = url,
            parsedRoute = parsedRoute,
            params = initialParams,
            source = source,
            timestamp = currentTimeMillis()
        )

        // 3. 通知观察者：路由开始
        observerManager.notifyRouteStart(context)

        // 4. 执行拦截器链 + 实际路由
        val terminalHandler = createTerminalHandler(options)
        val result = interceptorManager.executeChain(context, terminalHandler)

        // 5. 处理重定向
        val finalResult = handleRedirect(result, params, options, source)

        // 6. 通知观察者：路由完成
        observerManager.notifyRouteComplete(context, finalResult)

        return finalResult
    }

    /**
     * 检查是否可以打开指定 URL
     */
    fun canOpen(url: String): Boolean {
        val parsedRoute = ProtocolParser.parseOrNull(url) ?: return false
        return routeTable.canOpen(parsedRoute)
    }

    // ==================== 拦截器 API ====================

    /**
     * 添加全局拦截器
     */
    fun addGlobalInterceptor(interceptor: RouteInterceptor) {
        interceptorManager.addGlobalInterceptor(interceptor)
    }

    /**
     * 添加局部拦截器
     */
    fun addInterceptor(pattern: String, interceptor: RouteInterceptor) {
        interceptorManager.addInterceptor(pattern, interceptor)
    }

    /**
     * 移除全局拦截器
     */
    fun removeGlobalInterceptor(interceptor: RouteInterceptor): Boolean {
        return interceptorManager.removeGlobalInterceptor(interceptor)
    }

    // ==================== 观察者 API ====================

    /**
     * 添加路由观察者
     */
    fun addObserver(observer: RouteObserver) {
        observerManager.addObserver(observer)
    }

    /**
     * 移除路由观察者
     */
    fun removeObserver(observer: RouteObserver): Boolean {
        return observerManager.removeObserver(observer)
    }

    // ==================== 导航 API ====================

    /**
     * 返回上一页
     */
    fun pop(result: Any? = null) {
        navigator?.pop(result) ?: Logger.warn("Navigator not set, cannot pop")
    }

    /**
     * 返回到指定页面
     */
    fun popTo(pageId: String, result: Any? = null) {
        navigator?.popTo(pageId, result) ?: Logger.warn("Navigator not set, cannot popTo")
    }

    /**
     * 返回到根页面
     */
    fun popToRoot() {
        navigator?.popToRoot() ?: Logger.warn("Navigator not set, cannot popToRoot")
    }

    // ==================== 私有方法 ====================

    /**
     * 合并参数
     */
    private fun buildMergedParams(
        parsedRoute: ParsedRoute,
        extraParams: Map<String, Any?>
    ): Map<String, Any?> {
        // URL query 参数 + 额外参数
        val merged = mutableMapOf<String, Any?>()
        merged.putAll(parsedRoute.queryParams)
        merged.putAll(extraParams)
        return merged
    }

    /**
     * 创建终端处理器
     */
    private fun createTerminalHandler(options: NavigationOptions): RouteTerminalHandler {
        return RouteTerminalHandler { context ->
            executeRoute(context, options)
        }
    }

    /**
     * 执行实际路由
     */
    private fun executeRoute(context: RouteContext, options: NavigationOptions): RouteResult {
        val lookupResult = routeTable.lookup(context.parsedRoute)

        return when (lookupResult) {
            is RouteLookupResult.PageRoute -> {
                executePageRoute(context, lookupResult, options)
            }
            is RouteLookupResult.ActionRoute -> {
                executeActionRoute(context, lookupResult)
            }
            null -> {
                // 路由未找到，执行降级
                handleNotFound(context)
            }
        }
    }

    /**
     * 执行页面路由
     */
    private fun executePageRoute(
        context: RouteContext,
        lookupResult: RouteLookupResult.PageRoute,
        options: NavigationOptions
    ): RouteResult {
        val nav = navigator
        if (nav == null) {
            Logger.error("Navigator not set, cannot navigate to page")
            return RouteResult.Error(IllegalStateException("Navigator not set"))
        }

        val config = lookupResult.config
        val matchResult = lookupResult.matchResult

        // 合并 path 参数到 context params
        val finalParams = context.params.toMutableMap()
        finalParams.putAll(matchResult.pathParams)

        // 更新 context
        val finalContext = context.copy(params = finalParams)

        // 获取 pageId
        val pageId = config.pageId ?: config.pattern

        // 执行导航
        try {
            when (options.navMode) {
                NavMode.PUSH -> nav.push(pageId, finalParams, options)
                NavMode.PRESENT -> nav.present(pageId, finalParams, options)
            }
            return RouteResult.Success(finalContext)
        } catch (e: Exception) {
            Logger.error("Navigation failed", e)
            return RouteResult.Error(e)
        }
    }

    /**
     * 执行 Action 路由
     */
    private fun executeActionRoute(
        context: RouteContext,
        lookupResult: RouteLookupResult.ActionRoute
    ): RouteResult {
        val matchResult = lookupResult.matchResult
        val handler = lookupResult.handler

        // 合并 path 参数
        val finalParams = context.params.toMutableMap()
        finalParams.putAll(matchResult.pathParams)

        // 执行 Action
        try {
            // 直接执行 handler
            handler.execute(finalParams, object : ActionCallback {
                override fun onSuccess(result: Any?) {
                    Logger.debug("Action executed successfully: ${lookupResult.config.actionName}")
                }
                override fun onError(error: Throwable) {
                    Logger.error("Action execution failed: ${lookupResult.config.actionName}", error)
                }
            })
            return RouteResult.Success(context.copy(params = finalParams))
        } catch (e: Exception) {
            Logger.error("Action execution failed", e)
            return RouteResult.Error(e)
        }
    }

    /**
     * 处理路由未找到
     */
    private fun handleNotFound(context: RouteContext): RouteResult {
        val action = fallbackHandler.onRouteNotFound(context)
        executeFallbackAction(action, context)
        return RouteResult.NotFound(context.url)
    }

    /**
     * 执行降级动作
     */
    private fun executeFallbackAction(action: FallbackAction, context: RouteContext) {
        when (action) {
            is FallbackAction.NavigateTo -> {
                // 递归打开降级 URL（避免循环）
                if (action.url != context.url) {
                    open(action.url)
                }
            }
            is FallbackAction.ShowError -> {
                Logger.error("Route fallback: ${action.message}")
            }
            is FallbackAction.Ignore -> {
                // 不做处理
            }
            is FallbackAction.Custom -> {
                action.handler()
            }
        }
    }

    /**
     * 处理重定向
     */
    private suspend fun handleRedirect(
        result: RouteResult,
        params: Map<String, Any?>,
        options: NavigationOptions,
        source: RouteSource
    ): RouteResult {
        return when (result) {
            is RouteResult.Redirect -> {
                // 递归执行重定向
                val mergedParams = params + result.newParams
                openSuspend(result.newUrl, mergedParams, options, source)
            }
            else -> result
        }
    }

    /**
     * 处理路由结果，通知回调
     */
    private fun handleResult(result: RouteResult, url: String, callback: RouteCallback?) {
        if (callback == null) return

        when (result) {
            is RouteResult.Success -> {
                callback.onSuccess(result.context)
            }
            is RouteResult.Redirect -> {
                // 重定向已在 handleRedirect 中处理
            }
            is RouteResult.Blocked -> {
                callback.onError(RouteError(
                    code = RouteErrorCode.ROUTE_BLOCKED,
                    message = result.reason,
                    url = url
                ))
            }
            is RouteResult.Error -> {
                callback.onError(RouteError(
                    code = RouteErrorCode.UNKNOWN,
                    message = result.exception.message ?: "Unknown error",
                    url = url,
                    cause = result.exception
                ))
            }
            is RouteResult.NotFound -> {
                callback.onError(RouteError(
                    code = RouteErrorCode.ROUTE_NOT_FOUND,
                    message = "Route not found",
                    url = url
                ))
            }
        }
    }
}

/**
 * 获取当前时间戳（跨平台）
 */
internal expect fun currentTimeMillis(): Long
