package com.iap.router.observer

import com.iap.router.interceptor.RouteInterceptor
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.util.Logger

/**
 * 观察者管理器
 * 管理路由事件观察者的注册和通知
 *
 * 注意：所有操作应在主线程执行
 */
class ObserverManager {

    private val observers = mutableListOf<RouteObserver>()

    /**
     * 添加观察者
     * @param observer 路由观察者
     */
    fun addObserver(observer: RouteObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    /**
     * 移除观察者
     * @param observer 路由观察者
     * @return 是否移除成功
     */
    fun removeObserver(observer: RouteObserver): Boolean {
        return observers.remove(observer)
    }

    /**
     * 清除所有观察者
     */
    fun clearObservers() {
        observers.clear()
    }

    /**
     * 获取当前观察者数量
     */
    fun observerCount(): Int {
        return observers.size
    }

    /**
     * 通知路由开始
     */
    fun notifyRouteStart(context: RouteContext) {
        observers.toList().forEach { observer ->
            try {
                observer.onRouteStart(context)
            } catch (e: Exception) {
                Logger.error("Observer onRouteStart failed", e)
            }
        }
    }

    /**
     * 通知路由完成
     */
    fun notifyRouteComplete(context: RouteContext, result: RouteResult) {
        observers.toList().forEach { observer ->
            try {
                observer.onRouteComplete(context, result)
            } catch (e: Exception) {
                Logger.error("Observer onRouteComplete failed", e)
            }
        }
    }

    /**
     * 通知拦截器执行完成
     */
    fun notifyInterceptorExecuted(
        interceptor: RouteInterceptor,
        context: RouteContext,
        durationMs: Long
    ) {
        observers.toList().forEach { observer ->
            try {
                observer.onInterceptorExecuted(interceptor, context, durationMs)
            } catch (e: Exception) {
                Logger.error("Observer onInterceptorExecuted failed", e)
            }
        }
    }
}
