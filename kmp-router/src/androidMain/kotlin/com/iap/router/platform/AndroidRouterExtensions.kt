package com.iap.router.platform

import com.iap.router.Router

/**
 * Android 平台 Router 扩展
 */

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
