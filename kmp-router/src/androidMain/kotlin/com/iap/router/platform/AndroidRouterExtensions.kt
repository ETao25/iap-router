package com.iap.router.platform

import com.iap.router.Router

/**
 * Android 平台 Router 扩展
 */

/**
 * 初始化 Router
 *
 * 配置 Android 平台组件，调用此方法后 Router 即可执行 Android 页面导航和 Action
 *
 * 使用示例：
 * ```kotlin
 * Router.shared.initialize()
 * ```
 */
fun Router.initialize() {
    if (isInitialized()) {
        return
    }
    navigator = AndroidNavigator(routeTable)
    actionExecutor = AndroidActionExecutor(routeTable)
    markInitialized()
}

/**
 * 获取 Android Navigator
 */
fun Router.getAndroidNavigator(): AndroidNavigator? {
    return navigator as? AndroidNavigator
}

/**
 * 获取 Android ActionExecutor
 */
fun Router.getAndroidActionExecutor(): AndroidActionExecutor? {
    return actionExecutor as? AndroidActionExecutor
}
