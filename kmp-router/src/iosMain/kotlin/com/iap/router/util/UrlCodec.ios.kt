package com.iap.router.util

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.stringByRemovingPercentEncoding

actual object UrlCodec {
    actual fun encode(value: String): String {
        val allowedSet = NSCharacterSet.URLQueryAllowedCharacterSet.mutableCopy() as platform.Foundation.NSMutableCharacterSet
        allowedSet.removeCharactersInString("&=?")

        @Suppress("CAST_NEVER_SUCCEEDS")
        return (value as NSString)
            .stringByAddingPercentEncodingWithAllowedCharacters(allowedSet)
            ?: value
    }

    actual fun decode(value: String): String {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return (value as NSString).stringByRemovingPercentEncoding ?: value
    }
}
