package com.iap.router

import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig

/**
 * 路由定义（用于批量注册）
 */
sealed class RouteDefinition {
    /**
     * 页面路由定义
     * @param config 包含 pattern 的完整配置
     */
    data class Page(val config: PageRouteConfig) : RouteDefinition()

    data class Action(
        val actionName: String,
        val config: ActionRouteConfig,
        val handler: ActionHandler
    ) : RouteDefinition()
}

/**
 * 路由注册器
 * 平台特定的注册方法在各平台的 actual 实现中提供
 */
expect open class RouteRegistry(routeTable: RouteTable) {
    val routeTable: RouteTable

    /**
     * 注册页面路由
     * @param config 页面路由配置（包含 pattern）
     */
    fun registerPage(config: PageRouteConfig)

    /**
     * 注册 Action 路由
     * @param actionName Action 名称
     * @param handler Action 处理器
     */
    fun registerAction(actionName: String, handler: ActionHandler)

    /**
     * 注册 Action 路由（带配置）
     * @param actionName Action 名称
     * @param config Action 配置
     * @param handler Action 处理器
     */
    fun registerAction(actionName: String, config: ActionRouteConfig, handler: ActionHandler)

    /**
     * 批量注册路由
     */
    fun registerAll(routes: List<RouteDefinition>)

    /**
     * 检查路由是否已注册
     */
    fun isRegistered(pattern: String): Boolean

    /**
     * 移除页面路由
     */
    fun unregisterPage(pattern: String): Boolean

    /**
     * 移除 Action 路由
     */
    fun unregisterAction(actionName: String): Boolean
}
