package com.iap.router

import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig

/**
 * 路由定义（用于批量注册）
 */
sealed class RouteDefinition {
    data class Page(
        val pattern: String,
        val config: PageRouteConfig
    ) : RouteDefinition()

    data class Action(
        val actionName: String,
        val config: ActionRouteConfig,
        val handler: ActionHandler
    ) : RouteDefinition()
}

/**
 * 路由注册接口（核心）
 * 平台特定的注册方法在 iosMain/androidMain 中通过扩展函数提供
 */
interface RouteRegistry {
    /**
     * 注册页面路由（核心方法）
     * @param pattern 路由模式
     * @param config 页面路由配置
     */
    fun registerPage(pattern: String, config: PageRouteConfig)

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

    /**
     * 获取内部路由表（供平台扩展使用）
     */
    val routeTable: RouteTable
}

/**
 * 路由注册实现
 */
class RouteRegistryImpl(
    override val routeTable: RouteTable
) : RouteRegistry {

    override fun registerPage(pattern: String, config: PageRouteConfig) {
        routeTable.registerPage(pattern, config)
    }

    override fun registerAction(actionName: String, handler: ActionHandler) {
        registerAction(actionName, ActionRouteConfig(actionName), handler)
    }

    override fun registerAction(actionName: String, config: ActionRouteConfig, handler: ActionHandler) {
        routeTable.registerAction(actionName, config, handler)
    }

    override fun registerAll(routes: List<RouteDefinition>) {
        routes.forEach { definition ->
            when (definition) {
                is RouteDefinition.Page -> registerPage(definition.pattern, definition.config)
                is RouteDefinition.Action -> registerAction(
                    definition.actionName,
                    definition.config,
                    definition.handler
                )
            }
        }
    }

    override fun isRegistered(pattern: String): Boolean {
        return routeTable.contains(pattern)
    }

    override fun unregisterPage(pattern: String): Boolean {
        return routeTable.removePage(pattern)
    }

    override fun unregisterAction(actionName: String): Boolean {
        return routeTable.removeAction(actionName)
    }
}
