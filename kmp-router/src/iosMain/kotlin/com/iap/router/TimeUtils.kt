package com.iap.router

import com.iap.router.core.RouteTable
import com.iap.router.interceptor.InterceptorManager
import com.iap.router.observer.ObserverManager
import com.iap.router.platform.IOSActionExecutor
import com.iap.router.platform.IOSNavigator
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

internal actual fun createRouter(): Router {
    val routeTable = RouteTable()
    return Router(
        routeTable = routeTable,
        interceptorManager = InterceptorManager(),
        observerManager = ObserverManager(),
        navigator = IOSNavigator(routeTable),
        actionExecutor = IOSActionExecutor(routeTable)
    )
}
