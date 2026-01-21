package com.iap.router.params

import com.iap.router.exception.ParamValidationException

/**
 * 参数提取扩展函数
 * 提供类型安全的参数获取方式
 */

/**
 * 获取必需的 String 参数
 * @throws ParamValidationException 参数不存在时抛出
 */
fun Map<String, Any?>.requireString(key: String): String {
    return this[key]?.toString()
        ?: throw ParamValidationException(key, "Missing required param: $key")
}

/**
 * 获取可选的 String 参数
 */
fun Map<String, Any?>.optString(key: String, default: String? = null): String? {
    return this[key]?.toString() ?: default
}

/**
 * 获取必需的 Int 参数
 * @throws ParamValidationException 参数不存在或无法转换时抛出
 */
fun Map<String, Any?>.requireInt(key: String): Int {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
            ?: throw ParamValidationException(key, "Cannot convert '$value' to Int")
        else -> throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to Int")
    }
}

/**
 * 获取可选的 Int 参数
 */
fun Map<String, Any?>.optInt(key: String, default: Int = 0): Int {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: default
        else -> default
    }
}

/**
 * 获取必需的 Long 参数
 * @throws ParamValidationException 参数不存在或无法转换时抛出
 */
fun Map<String, Any?>.requireLong(key: String): Long {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
            ?: throw ParamValidationException(key, "Cannot convert '$value' to Long")
        else -> throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to Long")
    }
}

/**
 * 获取可选的 Long 参数
 */
fun Map<String, Any?>.optLong(key: String, default: Long = 0L): Long {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull() ?: default
        else -> default
    }
}

/**
 * 获取必需的 Double 参数
 * @throws ParamValidationException 参数不存在或无法转换时抛出
 */
fun Map<String, Any?>.requireDouble(key: String): Double {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
            ?: throw ParamValidationException(key, "Cannot convert '$value' to Double")
        else -> throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to Double")
    }
}

/**
 * 获取可选的 Double 参数
 */
fun Map<String, Any?>.optDouble(key: String, default: Double = 0.0): Double {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: default
        else -> default
    }
}

/**
 * 获取必需的 Float 参数
 * @throws ParamValidationException 参数不存在或无法转换时抛出
 */
fun Map<String, Any?>.requireFloat(key: String): Float {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return when (value) {
        is Number -> value.toFloat()
        is String -> value.toFloatOrNull()
            ?: throw ParamValidationException(key, "Cannot convert '$value' to Float")
        else -> throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to Float")
    }
}

/**
 * 获取可选的 Float 参数
 */
fun Map<String, Any?>.optFloat(key: String, default: Float = 0.0f): Float {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toFloat()
        is String -> value.toFloatOrNull() ?: default
        else -> default
    }
}

/**
 * 获取必需的 Boolean 参数
 * @throws ParamValidationException 参数不存在时抛出
 */
fun Map<String, Any?>.requireBoolean(key: String): Boolean {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return when (value) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull()
            ?: throw ParamValidationException(key, "Cannot convert '$value' to Boolean")
        is Number -> value.toInt() != 0
        else -> throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to Boolean")
    }
}

/**
 * 获取可选的 Boolean 参数
 */
fun Map<String, Any?>.optBoolean(key: String, default: Boolean = false): Boolean {
    val value = this[key] ?: return default
    return when (value) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull() ?: default
        is Number -> value.toInt() != 0
        else -> default
    }
}

/**
 * 获取可选的 List 参数
 */
@Suppress("UNCHECKED_CAST")
fun <T> Map<String, Any?>.optList(key: String): List<T>? {
    return this[key] as? List<T>
}

/**
 * 获取必需的 List 参数
 * @throws ParamValidationException 参数不存在或类型不匹配时抛出
 */
@Suppress("UNCHECKED_CAST")
fun <T> Map<String, Any?>.requireList(key: String): List<T> {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return (value as? List<T>)
        ?: throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to List")
}

/**
 * 获取可选的 Map 参数
 */
@Suppress("UNCHECKED_CAST")
fun <K, V> Map<String, Any?>.optMap(key: String): Map<K, V>? {
    return this[key] as? Map<K, V>
}

/**
 * 获取必需的 Map 参数
 * @throws ParamValidationException 参数不存在或类型不匹配时抛出
 */
@Suppress("UNCHECKED_CAST")
fun <K, V> Map<String, Any?>.requireMap(key: String): Map<K, V> {
    val value = this[key]
        ?: throw ParamValidationException(key, "Missing required param: $key")
    return (value as? Map<K, V>)
        ?: throw ParamValidationException(key, "Cannot convert '${value::class.simpleName}' to Map")
}

/**
 * 类型安全的参数接口
 * 各路由可定义对应的参数类实现此接口
 */
interface RouteParams {
    /**
     * 将参数转换为 Map
     */
    fun toMap(): Map<String, Any?>
}
