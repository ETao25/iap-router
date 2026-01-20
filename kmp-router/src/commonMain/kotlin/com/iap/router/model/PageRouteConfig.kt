package com.iap.router.model

import com.iap.router.fallback.FallbackConfig

/**
 * 页面路由配置
 */
data class PageRouteConfig(
    /**
     * 页面业务标识符
     * 用途：
     * 1. 与 VC/Activity 类名或注册标识对应
     * 2. 埋点、日志中的页面标识
     * 3. 白名单/黑名单控制
     * 注：pageId 与 pattern 独立，pattern 是 URL 路径匹配模式
     */
    val pageId: String,

    /**
     * 单路由降级配置
     */
    val fallback: FallbackConfig? = null
)

/**
 * Action 路由配置
 */
data class ActionRouteConfig(
    /**
     * Action 名称标识
     */
    val actionName: String
)
