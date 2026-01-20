package com.iap.router.model

import com.iap.router.params.ObjectStore

/**
 * 路由上下文
 * 包含路由执行过程中的所有信息
 */
data class RouteContext(
    /** 原始 URL */
    val url: String,
    /** 解析后的路由信息 */
    val parsedRoute: ParsedRoute,
    /** 合并后的参数（只读） */
    val params: Map<String, Any?>,
    /** 路由来源 */
    val source: RouteSource,
    /** 时间戳 */
    val timestamp: Long,
    /** 内部对象存储（用于 extras 中的对象引用） */
    internal val objectStore: ObjectStore = ObjectStore()
) {
    /**
     * 获取对象引用（会从 objectStore 中取出）
     * 路由完成后 objectStore 会被清理
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getObject(key: String): T? = objectStore.get(key) as? T

    /**
     * 创建带有额外参数的新 Context
     */
    fun withParams(extraParams: Map<String, Any?>): RouteContext {
        return copy(params = params + extraParams)
    }

    /**
     * 清理对象存储
     */
    fun cleanup() {
        objectStore.clear()
    }
}
