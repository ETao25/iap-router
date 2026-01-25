package com.iap.router.platform

/**
 * 平台页面创建器标记接口
 * 各平台在 iosMain/androidMain 中定义具体实现
 */
interface PlatformPageCreator

/**
 * 页面目标（平台无关）
 * 存储平台特定的页面创建器
 */
data class PageTarget(
    /**
     * 平台特定的页面创建器
     * iOS: IOSPageCreator
     * Android: AndroidPageCreator
     */
    val creator: PlatformPageCreator
)
