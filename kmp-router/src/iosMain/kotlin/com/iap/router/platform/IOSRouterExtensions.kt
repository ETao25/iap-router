package com.iap.router.platform

import com.iap.router.Router

/**
 * iOS 平台 Router 扩展
 */

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
