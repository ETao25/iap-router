package com.iap.router

import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig

/**
 * JVM 路由注册器实现（用于测试）
 */
actual open class RouteRegistry actual constructor(
    actual val routeTable: RouteTable
) {
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
}
