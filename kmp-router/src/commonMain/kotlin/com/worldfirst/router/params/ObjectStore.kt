package com.worldfirst.router.params

/**
 * 对象存储
 * 用于在路由过程中传递对象引用（如 ViewModel）
 * 路由完成后会自动清理
 */
class ObjectStore {
    private val store = mutableMapOf<String, Any?>()

    /**
     * 存储对象
     */
    fun put(key: String, value: Any?) {
        store[key] = value
    }

    /**
     * 获取对象
     */
    fun get(key: String): Any? = store[key]

    /**
     * 获取并移除对象
     */
    fun remove(key: String): Any? = store.remove(key)

    /**
     * 清空所有存储的对象
     */
    fun clear() {
        store.clear()
    }

    /**
     * 检查是否包含指定 key
     */
    fun contains(key: String): Boolean = store.containsKey(key)

    /**
     * 获取所有存储的 key
     */
    fun keys(): Set<String> = store.keys.toSet()
}
