package com.iap.router.platform

import com.iap.router.Router

/**
 * iOS 平台 Router 扩展
 */

/**
 * 配置 iOS 平台组件
 *
 * 调用此方法后，Router 即可执行 iOS 页面导航和 Action
 *
 * 使用示例：
 * ```kotlin
 * val router = Router()
 * router.setupIOSPlatform()
 * ```
 */
fun Router.setupIOSPlatform() {
    navigator = IOSNavigator(routeTable)
    actionExecutor = IOSActionExecutor(routeTable)
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
