package com.iap.router.platform

import com.iap.router.Router

/**
 * iOS 平台 Router 扩展
 */

/**
 * 初始化 Router
 *
 * 配置 iOS 平台组件，调用此方法后 Router 即可执行 iOS 页面导航和 Action
 *
 * 使用示例（Swift）：
 * ```swift
 * Router.shared.initialize()
 * ```
 *
 * 使用示例（Kotlin）：
 * ```kotlin
 * Router.shared.initialize()
 * ```
 */
fun Router.initialize() {
    if (isInitialized()) {
        return
    }
    navigator = IOSNavigator(routeTable)
    actionExecutor = IOSActionExecutor(routeTable)
    markInitialized()
}

/**
 * 获取 iOS Navigator
 */
fun Router.getIOSNavigator(): IOSNavigator? {
    return navigator as? IOSNavigator
}

/**
 * 获取 iOS ActionExecutor
 */
fun Router.getIOSActionExecutor(): IOSActionExecutor? {
    return actionExecutor as? IOSActionExecutor
}
