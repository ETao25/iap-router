package com.iap.router

import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig
import com.iap.router.platform.IOSPageCreator
import com.iap.router.platform.PageTarget
import platform.UIKit.UIViewController

/**
 * iOS 路由注册器实现
 * 包含 iOS 特定的页面注册方法
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

    // ==================== iOS 特定方法 ====================

    /**
     * 注册页面路由（iOS 工厂函数方式）
     *
     * Swift 调用示例:
     * ```swift
     * registry.registerPage(pattern: "order/detail/:orderId") { params in
     *     OrderDetailViewController(params: params)
     * }
     * ```
     */
    fun registerPage(
        pattern: String,
        factory: (Map<String, Any?>) -> UIViewController
    ) {
        val creator = IOSPageCreator(factory)
        val config = PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = null
        )
        registerPage(config)
    }

    /**
     * 注册页面路由（iOS 工厂函数 + 自定义 pageId）
     */
    fun registerPage(
        pattern: String,
        pageId: String,
        factory: (Map<String, Any?>) -> UIViewController
    ) {
        val creator = IOSPageCreator(factory)
        val config = PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = pageId
        )
        registerPage(config)
    }
}
