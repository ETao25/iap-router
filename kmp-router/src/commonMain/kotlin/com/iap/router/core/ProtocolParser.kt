package com.iap.router.core

import com.iap.router.model.ParsedRoute
import com.iap.router.util.UrlCodec

/**
 * 协议解析器
 * 负责将路由 URL 解析为结构化的 ParsedRoute 对象
 *
 * 支持的协议格式：
 * - iap://path/to/page?param1=value1&param2=value2
 * - iap://order/detail/123?from=list
 */
object ProtocolParser {

    private const val DEFAULT_SCHEME = "iap"

    /**
     * 解析路由 URL
     * 使用 Result 包装，调用方通过 getOrNull/getOrElse 处理
     *
     * @param url 待解析的 URL
     * @return Result<ParsedRoute> 解析结果
     */
    fun parse(url: String): Result<ParsedRoute> {
        return runCatching {
            parseInternal(url)
        }
    }

    /**
     * 安全解析，失败返回 null
     */
    fun parseOrNull(url: String): ParsedRoute? = parse(url).getOrNull()

    /**
     * 内部解析实现
     */
    private fun parseInternal(url: String): ParsedRoute {
        val trimmedUrl = url.trim()
        require(trimmedUrl.isNotEmpty()) { "URL cannot be empty" }

        // 查找 scheme 分隔符
        val schemeEndIndex = trimmedUrl.indexOf("://")
        require(schemeEndIndex > 0) { "Invalid URL format: missing scheme separator '://'" }

        val scheme = trimmedUrl.substring(0, schemeEndIndex)
        require(scheme.isNotEmpty()) { "Scheme cannot be empty" }

        // 提取 scheme 后面的部分
        val remainder = trimmedUrl.substring(schemeEndIndex + 3)

        // 分离 path 和 query
        val queryStartIndex = remainder.indexOf('?')
        val (path, queryString) = if (queryStartIndex >= 0) {
            remainder.substring(0, queryStartIndex) to remainder.substring(queryStartIndex + 1)
        } else {
            remainder to ""
        }

        // 解析 query 参数
        val queryParams = parseQueryString(queryString)

        return ParsedRoute(
            scheme = scheme,
            path = path.trimStart('/'),  // 移除开头的斜杠
            queryParams = queryParams,
            pathParams = mutableMapOf()
        )
    }

    /**
     * 解析 query 字符串
     * 支持 URL 编码的参数值
     */
    private fun parseQueryString(queryString: String): Map<String, String> {
        if (queryString.isEmpty()) return emptyMap()

        return queryString
            .split('&')
            .filter { it.isNotEmpty() }
            .mapNotNull { pair ->
                val eqIndex = pair.indexOf('=')
                if (eqIndex > 0) {
                    val key = UrlCodec.decode(pair.substring(0, eqIndex))
                    val value = if (eqIndex < pair.length - 1) {
                        UrlCodec.decode(pair.substring(eqIndex + 1))
                    } else {
                        ""
                    }
                    key to value
                } else if (eqIndex == -1 && pair.isNotEmpty()) {
                    // 无值参数，如 ?flag
                    UrlCodec.decode(pair) to ""
                } else {
                    null
                }
            }
            .toMap()
    }

    /**
     * URL 编码
     */
    fun encodeUrlComponent(value: String): String {
        return UrlCodec.encode(value)
    }

    /**
     * 构建完整 URL
     *
     * @param scheme 协议
     * @param path 路径
     * @param queryParams query 参数
     * @return 完整 URL 字符串
     */
    fun buildUrl(
        scheme: String = DEFAULT_SCHEME,
        path: String,
        queryParams: Map<String, String> = emptyMap()
    ): String {
        val normalizedPath = path.trimStart('/')
        val queryString = if (queryParams.isNotEmpty()) {
            "?" + queryParams.entries.joinToString("&") { (key, value) ->
                "${UrlCodec.encode(key)}=${UrlCodec.encode(value)}"
            }
        } else {
            ""
        }
        return "$scheme://$normalizedPath$queryString"
    }
}
