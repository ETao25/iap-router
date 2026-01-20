package com.iap.router.util

/**
 * URL 编解码工具
 * 使用 expect/actual 模式实现平台特定的编解码
 */
expect object UrlCodec {
    /**
     * URL 编码
     * @param value 待编码的字符串
     * @return 编码后的字符串
     */
    fun encode(value: String): String

    /**
     * URL 解码
     * @param value 待解码的字符串
     * @return 解码后的字符串
     */
    fun decode(value: String): String
}
