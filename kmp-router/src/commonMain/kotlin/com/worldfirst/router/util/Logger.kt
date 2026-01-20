package com.worldfirst.router.util

/**
 * 日志接口
 * 由业务层实现，注入到 SDK 中
 */
interface RouterLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * 日志单例
 * SDK 内部使用，业务层通过 setLogger 注入实现
 */
object Logger {
    private const val TAG = "[Router]"

    private var impl: RouterLogger? = null

    /**
     * 设置日志实现
     */
    fun setLogger(logger: RouterLogger) {
        impl = logger
    }

    /**
     * 清除日志实现
     */
    fun clearLogger() {
        impl = null
    }

    /**
     * Debug 级别日志
     */
    fun debug(message: String) {
        impl?.debug("$TAG $message")
    }

    /**
     * Info 级别日志
     */
    fun info(message: String) {
        impl?.info("$TAG $message")
    }

    /**
     * Warn 级别日志
     */
    fun warn(message: String, throwable: Throwable? = null) {
        impl?.warn("$TAG $message", throwable)
    }

    /**
     * Error 级别日志
     */
    fun error(message: String, throwable: Throwable? = null) {
        impl?.error("$TAG $message", throwable)
    }
}

/**
 * 默认日志实现（使用 println）
 * 仅用于开发调试，生产环境应注入正式的日志实现
 */
class DefaultLogger : RouterLogger {
    override fun debug(message: String) {
        println("DEBUG: $message")
    }

    override fun info(message: String) {
        println("INFO: $message")
    }

    override fun warn(message: String, throwable: Throwable?) {
        println("WARN: $message")
        throwable?.let { println(it.stackTraceToString()) }
    }

    override fun error(message: String, throwable: Throwable?) {
        println("ERROR: $message")
        throwable?.let { println(it.stackTraceToString()) }
    }
}
