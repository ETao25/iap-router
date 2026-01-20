package com.iap.router.model

/**
 * 路由来源枚举
 */
enum class RouteSource {
    /** 应用内部路由调用 */
    INTERNAL,
    /** DeepLink 外部跳转 */
    DEEPLINK,
    /** 推送消息跳转 */
    PUSH,
    /** H5 页面跳转 */
    H5,
    /** 其他来源 */
    OTHER
}
