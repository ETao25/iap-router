package com.iap.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig
import com.iap.router.platform.AndroidPageCreator
import com.iap.router.platform.PageTarget
import kotlin.reflect.KClass

/**
 * Android 路由注册器实现
 * 包含 Android 特定的页面注册方法
 */
actual open class RouteRegistry actual constructor(
    actual val routeTable: RouteTable
) {
    // ==================== 通用方法 ====================

    actual fun registerPage(config: PageRouteConfig) {
        routeTable.registerPage(config)
    }

    actual fun registerAction(actionName: String, handler: ActionHandler) {
        registerAction(actionName, ActionRouteConfig(actionName), handler)
    }

    actual fun registerAction(actionName: String, config: ActionRouteConfig, handler: ActionHandler) {
        routeTable.registerAction(actionName, config, handler)
    }

    actual fun registerAll(routes: List<RouteDefinition>) {
        routes.forEach { definition ->
            when (definition) {
                is RouteDefinition.Page -> registerPage(definition.config)
                is RouteDefinition.Action -> registerAction(
                    definition.actionName,
                    definition.config,
                    definition.handler
                )
            }
        }
    }

    actual fun isRegistered(pattern: String): Boolean {
        return routeTable.contains(pattern)
    }

    actual fun unregisterPage(pattern: String): Boolean {
        return routeTable.removePage(pattern)
    }

    actual fun unregisterAction(actionName: String): Boolean {
        return routeTable.removeAction(actionName)
    }

    // ==================== Android 特定方法 ====================

    /**
     * 注册页面路由（通过 Activity KClass）
     *
     * Kotlin 调用示例:
     * ```kotlin
     * registry.registerPage("order/detail/:orderId", OrderDetailActivity::class)
     * ```
     */
    fun registerPage(pattern: String, activityClass: KClass<out Activity>) {
        val creator = AndroidPageCreator(activityClass)
        val config = PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = null
        )
        registerPage(config)
    }

    /**
     * 注册页面路由（通过 Intent 工厂函数）
     *
     * Kotlin 调用示例:
     * ```kotlin
     * registry.registerPage("order/detail/:orderId") { context, params ->
     *     Intent(context, OrderDetailActivity::class.java).apply {
     *         putExtra("orderId", params["orderId"] as? String)
     *     }
     * }
     * ```
     */
    fun registerPage(pattern: String, intentFactory: (Context, Map<String, Any?>) -> Intent) {
        val creator = AndroidPageCreator(intentFactory)
        val config = PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = null
        )
        registerPage(config)
    }

    /**
     * 便捷方法：使用 reified 类型参数注册页面
     *
     * Kotlin 调用示例:
     * ```kotlin
     * registry.registerPage<OrderDetailActivity>("order/detail/:orderId")
     * ```
     */
    inline fun <reified T : Activity> registerPage(pattern: String) {
        registerPage(pattern, T::class)
    }
}
