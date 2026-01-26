package com.iap.router

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS 平台时间戳实现
 */
internal actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
