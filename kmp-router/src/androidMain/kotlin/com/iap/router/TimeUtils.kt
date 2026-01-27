package com.iap.router

import com.iap.router.core.RouteTable
import com.iap.router.interceptor.InterceptorManager
import com.iap.router.observer.ObserverManager
import com.iap.router.platform.AndroidActionExecutor
import com.iap.router.platform.AndroidNavigator

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

internal actual fun createRouter(): Router {
    val routeTable = RouteTable()
    return Router(
        routeTable = routeTable,
        interceptorManager = InterceptorManager(),
        observerManager = ObserverManager(),
        navigator = AndroidNavigator(routeTable),
        actionExecutor = AndroidActionExecutor(routeTable)
    )
}
