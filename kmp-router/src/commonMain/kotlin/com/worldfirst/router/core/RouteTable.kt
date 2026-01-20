package com.worldfirst.router.core

import com.worldfirst.router.model.ActionRouteConfig
import com.worldfirst.router.model.PageRouteConfig
import com.worldfirst.router.model.ParsedRoute

/**
 * 路由条目（内部使用）
 */
internal sealed class RouteEntry {
    abstract val pattern: String

    data class Page(
        override val pattern: String,
        val config: PageRouteConfig
    ) : RouteEntry()

    data class Action(
        override val pattern: String,
        val config: ActionRouteConfig,
        val handler: ActionHandler
    ) : RouteEntry()
}

/**
 * Action 处理器接口
 */
interface ActionHandler {
    /**
     * 执行 Action
     * @param params 合并后的参数
     * @param callback 执行结果回调
     */
    fun execute(params: Map<String, Any?>, callback: ActionCallback?)
}

/**
 * Action 回调接口
 */
interface ActionCallback {
    fun onSuccess(result: Any?)
    fun onError(error: Throwable)
}

/**
 * 路由查找结果
 */
sealed class RouteLookupResult {
    data class PageRoute(
        val config: PageRouteConfig,
        val matchResult: RouteMatchResult
    ) : RouteLookupResult()

    data class ActionRoute(
        val config: ActionRouteConfig,
        val handler: ActionHandler,
        val matchResult: RouteMatchResult
    ) : RouteLookupResult()
}

/**
 * 路由表
 * 管理所有注册的路由
 */
class RouteTable {

    // 页面路由表：pattern -> RouteEntry.Page
    private val pageRoutes = mutableMapOf<String, RouteEntry.Page>()

    // Action 路由表：pattern -> RouteEntry.Action
    private val actionRoutes = mutableMapOf<String, RouteEntry.Action>()

    /**
     * 注册页面路由
     * @param pattern 路由模式，如 "order/detail/:orderId"
     * @param config 页面路由配置
     */
    fun registerPage(pattern: String, config: PageRouteConfig) {
        val normalizedPattern = normalizePattern(pattern)
        pageRoutes[normalizedPattern] = RouteEntry.Page(normalizedPattern, config)
    }

    /**
     * 注册 Action 路由
     * @param actionName Action 名称（会自动加上 action/ 前缀）
     * @param config Action 配置
     * @param handler Action 处理器
     */
    fun registerAction(actionName: String, config: ActionRouteConfig, handler: ActionHandler) {
        val pattern = "action/$actionName"
        actionRoutes[pattern] = RouteEntry.Action(pattern, config, handler)
    }

    /**
     * 根据解析后的路由查找匹配的路由配置
     * @param parsedRoute 解析后的路由
     * @return 查找结果，未找到返回 null
     */
    fun lookup(parsedRoute: ParsedRoute): RouteLookupResult? {
        val path = parsedRoute.path

        // 优先检查 Action 路由
        if (path.startsWith("action/")) {
            val actionMatch = RouteMatcher.findBestMatch(path, actionRoutes.keys)
            if (actionMatch != null) {
                val entry = actionRoutes[actionMatch.pattern]!!
                return RouteLookupResult.ActionRoute(entry.config, entry.handler, actionMatch)
            }
        }

        // 检查页面路由
        val pageMatch = RouteMatcher.findBestMatch(path, pageRoutes.keys)
        if (pageMatch != null) {
            val entry = pageRoutes[pageMatch.pattern]!!
            return RouteLookupResult.PageRoute(entry.config, pageMatch)
        }

        return null
    }

    /**
     * 检查路由是否已注册
     */
    fun contains(pattern: String): Boolean {
        val normalized = normalizePattern(pattern)
        return pageRoutes.containsKey(normalized) || actionRoutes.containsKey(normalized)
    }

    /**
     * 检查是否可以打开指定路由
     */
    fun canOpen(parsedRoute: ParsedRoute): Boolean {
        return lookup(parsedRoute) != null
    }

    /**
     * 获取所有已注册的页面路由模式
     */
    fun getPagePatterns(): Set<String> = pageRoutes.keys.toSet()

    /**
     * 获取所有已注册的 Action 路由模式
     */
    fun getActionPatterns(): Set<String> = actionRoutes.keys.toSet()

    /**
     * 获取页面路由配置
     */
    fun getPageConfig(pattern: String): PageRouteConfig? {
        return pageRoutes[normalizePattern(pattern)]?.config
    }

    /**
     * 移除页面路由
     */
    fun removePage(pattern: String): Boolean {
        return pageRoutes.remove(normalizePattern(pattern)) != null
    }

    /**
     * 移除 Action 路由
     */
    fun removeAction(actionName: String): Boolean {
        return actionRoutes.remove("action/$actionName") != null
    }

    /**
     * 清空所有路由
     */
    fun clear() {
        pageRoutes.clear()
        actionRoutes.clear()
    }

    /**
     * 获取注册的路由数量
     */
    fun size(): Int = pageRoutes.size + actionRoutes.size

    /**
     * 标准化模式（移除开头的斜杠）
     */
    private fun normalizePattern(pattern: String): String {
        return pattern.trimStart('/')
    }
}
