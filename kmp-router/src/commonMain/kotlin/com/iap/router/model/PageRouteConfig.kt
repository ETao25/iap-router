package com.iap.router.model

import com.iap.router.platform.PageTarget

/**
 * 页面路由配置
 * 包含路由模式、页面创建器和可选的页面标识符
 */
data class PageRouteConfig(
    /**
     * 路由模式
     * 如 "order/detail/:orderId", "user/profile"
     */
    val pattern: String,

    /**
     * 页面目标：存储平台特定的页面创建器
     */
    val target: PageTarget,

    /**
     * 页面业务标识符（可选）
     * 用途：埋点、日志中的页面标识
     * 如果不指定，默认使用 pattern
     */
    val pageId: String? = null
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
