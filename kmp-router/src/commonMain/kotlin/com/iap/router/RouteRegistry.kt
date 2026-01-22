package com.iap.router

import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.fallback.FallbackConfig
import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig
import com.iap.router.platform.PageBuilder
import com.iap.router.platform.PageTarget
import com.iap.router.platform.PlatformPage
import kotlin.reflect.KClass

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
 * 路由注册接口
 */
interface RouteRegistry {
    /**
     * 注册页面路由（通过 builder）
     * @param pattern 路由模式，如 "order/detail/:orderId"
     * @param builder 页面构建器（类型安全，返回 PlatformPage）
     * @param fallback 可选的降级配置
     */
    fun registerPage(
        pattern: String,
        builder: PageBuilder,
        fallback: FallbackConfig? = null
    )

    /**
     * 注册页面路由（通过 class）
     * @param pattern 路由模式，如 "order/detail/:orderId"
     * @param pageClass 页面类（类型安全，必须是 PlatformPage 子类）
     * @param fallback 可选的降级配置
     */
    fun <T : PlatformPage> registerPage(
        pattern: String,
        pageClass: KClass<T>,
        fallback: FallbackConfig? = null
    )

    /**
     * 注册页面路由（完整配置）
     * @param pattern 路由模式
     * @param config 完整的页面路由配置
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
}

/**
 * 路由注册实现
 */
class RouteRegistryImpl(
    private val routeTable: RouteTable
) : RouteRegistry {

    override fun registerPage(
        pattern: String,
        builder: PageBuilder,
        fallback: FallbackConfig?
    ) {
        routeTable.registerPage(pattern, builder, fallback)
    }

    override fun <T : PlatformPage> registerPage(
        pattern: String,
        pageClass: KClass<T>,
        fallback: FallbackConfig?
    ) {
        routeTable.registerPage(pattern, pageClass, fallback)
    }

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

/**
 * 便捷扩展：使用 reified 类型参数注册页面
 */
inline fun <reified T : PlatformPage> RouteRegistry.registerPage(
    pattern: String,
    fallback: FallbackConfig? = null
) {
    registerPage(pattern, T::class, fallback)
}
