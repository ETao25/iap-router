package com.iap.router.util

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLParameter

/**
 * URL 编解码工具
 * 使用 Ktor HTTP 提供的跨平台实现
 */
object UrlCodec {
    /**
     * URL 编码
     * 将特殊字符转换为 %XX 格式
     */
    fun encode(value: String): String {
        return value.encodeURLParameter()
    }

    /**
     * URL 解码（用于 query 参数）
     * 将 %XX 格式还原为原始字符，同时将 + 解码为空格
     */
    fun decode(value: String): String {
        return value.decodeURLQueryComponent(plusIsSpace = true)
    }
}
