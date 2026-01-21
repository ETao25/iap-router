package com.iap.router.observer

import com.iap.router.interceptor.RouteInterceptor
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.util.Logger

/**
 * 观察者管理器
 * 管理路由事件观察者的注册和通知
 */
class ObserverManager {

    private val observers = mutableListOf<RouteObserver>()
    private val lock = Any()

    /**
     * 添加观察者
     * @param observer 路由观察者
     */
    fun addObserver(observer: RouteObserver) {
        synchronized(lock) {
            if (!observers.contains(observer)) {
                observers.add(observer)
            }
        }
    }

    /**
     * 移除观察者
     * @param observer 路由观察者
     * @return 是否移除成功
     */
    fun removeObserver(observer: RouteObserver): Boolean {
        synchronized(lock) {
            return observers.remove(observer)
        }
    }

    /**
     * 清除所有观察者
     */
    fun clearObservers() {
        synchronized(lock) {
            observers.clear()
        }
    }

    /**
     * 获取当前观察者数量
     */
    fun observerCount(): Int {
        synchronized(lock) {
            return observers.size
        }
    }

    /**
     * 通知路由开始
     */
    fun notifyRouteStart(context: RouteContext) {
        getObserversCopy().forEach { observer ->
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
        getObserversCopy().forEach { observer ->
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
        getObserversCopy().forEach { observer ->
            try {
                observer.onInterceptorExecuted(interceptor, context, durationMs)
            } catch (e: Exception) {
                Logger.error("Observer onInterceptorExecuted failed", e)
            }
        }
    }

    /**
     * 获取观察者列表的副本（避免并发修改）
     */
    private fun getObserversCopy(): List<RouteObserver> {
        synchronized(lock) {
            return observers.toList()
        }
    }
}
