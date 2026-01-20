package com.iap.router.util

import java.net.URLDecoder
import java.net.URLEncoder

actual object UrlCodec {
    actual fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
            .replace("+", "%20")
    }

    actual fun decode(value: String): String {
        return try {
            URLDecoder.decode(value, Charsets.UTF_8.name())
        } catch (e: Exception) {
            value
        }
    }
}
