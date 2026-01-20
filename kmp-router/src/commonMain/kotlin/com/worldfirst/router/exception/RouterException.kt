package com.worldfirst.router.exception

/**
 * 路由 SDK 异常基类
 * 所有 SDK 异常都应继承此类
 */
open class RouterException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 路由未找到异常
 */
class RouteNotFoundException(
    val url: String,
    message: String = "Route not found: $url"
) : RouterException(message)

/**
 * 路由被拦截异常
 */
class RouteBlockedException(
    val url: String,
    val reason: String,
    message: String = "Route blocked: $url, reason: $reason"
) : RouterException(message)

/**
 * 参数校验异常
 */
class ParamValidationException(
    val paramName: String,
    message: String = "Invalid or missing param: $paramName"
) : RouterException(message)

/**
 * 导航执行异常
 */
class NavigationException(
    message: String,
    cause: Throwable? = null
) : RouterException(message, cause)

/**
 * 协议解析异常
 */
class ProtocolParseException(
    val url: String,
    message: String = "Failed to parse url: $url",
    cause: Throwable? = null
) : RouterException(message, cause)
