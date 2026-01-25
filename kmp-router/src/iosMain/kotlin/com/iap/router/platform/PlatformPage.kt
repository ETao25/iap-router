package com.iap.router.platform

import com.iap.router.RouteRegistry
import com.iap.router.fallback.FallbackConfig
import com.iap.router.model.PageRouteConfig
import platform.UIKit.UIViewController

/**
 * iOS 页面创建器
 * 通过工厂函数创建 UIViewController
 */
class IOSPageCreator(
    /**
     * 工厂函数：接收参数，返回 UIViewController
     */
    val factory: (Map<String, Any?>) -> UIViewController
) : PlatformPageCreator {

    /**
     * 创建 ViewController 实例
     */
    fun createViewController(params: Map<String, Any?>): UIViewController {
        return factory.invoke(params)
    }
}

// ==================== iOS 专用扩展函数 ====================

/**
 * 注册页面路由（通过工厂函数）
 * Swift 调用示例:
 * ```swift
 * registry.registerPage(pattern: "order/detail/:orderId") { params in
 *     OrderDetailViewController(params: params)
 * }
 * ```
 */
fun RouteRegistry.registerPage(
    pattern: String,
    factory: (Map<String, Any?>) -> UIViewController
) {
    val creator = IOSPageCreator(factory)
    val config = PageRouteConfig(
        target = PageTarget(creator),
        pageId = null,
        fallback = null
    )
    registerPage(pattern, config)
}

/**
 * 注册页面路由（通过工厂函数 + 降级配置）
 */
fun RouteRegistry.registerPage(
    pattern: String,
    fallback: FallbackConfig?,
    factory: (Map<String, Any?>) -> UIViewController
) {
    val creator = IOSPageCreator(factory)
    val config = PageRouteConfig(
        target = PageTarget(creator),
        pageId = null,
        fallback = fallback
    )
    registerPage(pattern, config)
}

/**
 * 注册页面路由（通过工厂函数 + 自定义 pageId）
 */
fun RouteRegistry.registerPage(
    pattern: String,
    pageId: String,
    factory: (Map<String, Any?>) -> UIViewController
) {
    val creator = IOSPageCreator(factory)
    val config = PageRouteConfig(
        target = PageTarget(creator),
        pageId = pageId,
        fallback = null
    )
    registerPage(pattern, config)
}

/**
 * 注册页面路由（完整配置）
 */
fun RouteRegistry.registerPage(
    pattern: String,
    pageId: String?,
    fallback: FallbackConfig?,
    factory: (Map<String, Any?>) -> UIViewController
) {
    val creator = IOSPageCreator(factory)
    val config = PageRouteConfig(
        target = PageTarget(creator),
        pageId = pageId,
        fallback = fallback
    )
    registerPage(pattern, config)
}

// ==================== 辅助函数 ====================

/**
 * 从 PageRouteConfig 获取 iOS 页面创建器
 */
fun PageRouteConfig.getIOSPageCreator(): IOSPageCreator? {
    return target.creator as? IOSPageCreator
}

/**
 * 创建 UIViewController
 */
fun PageRouteConfig.createViewController(params: Map<String, Any?>): UIViewController? {
    return getIOSPageCreator()?.createViewController(params)
}
