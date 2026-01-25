package com.iap.router.model

import com.iap.router.fallback.FallbackConfig
import com.iap.router.platform.PageTarget

/**
 * 页面路由配置
 */
data class PageRouteConfig(
    /**
     * 页面目标：存储平台特定的页面创建器
     */
    val target: PageTarget,

    /**
     * 页面业务标识符（可选）
     * 用途：埋点、日志中的页面标识
     * 如果不指定，默认使用注册时的 pattern
     */
    val pageId: String? = null,

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
