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

// ==================== 声明式路由注册 ====================

/**
 * 页面路由定义
 * 用于声明式路由注册
 *
 * Swift 使用示例:
 * ```swift
 * // 1. 定义 Swift 协议
 * protocol PageRoutable {
 *     static var pattern: String { get }
 *     static func createPage(params: [String: Any?]) -> UIViewController
 *     static var fallback: FallbackConfig? { get }
 * }
 *
 * extension PageRoutable {
 *     static var fallback: FallbackConfig? { nil }
 *
 *     static var routeDefinition: PageRouteDefinition {
 *         PageRouteDefinition(
 *             pattern: pattern,
 *             fallback: fallback,
 *             factory: { params in createPage(params: params) }
 *         )
 *     }
 * }
 *
 * // 2. ViewController 实现协议
 * class OrderDetailViewController: UIViewController, PageRoutable {
 *     static var pattern: String { "order/detail/:orderId" }
 *
 *     static func createPage(params: [String: Any?]) -> UIViewController {
 *         OrderDetailViewController(orderId: params["orderId"] as? String)
 *     }
 * }
 *
 * // 3. 注册（一行）
 * registry.registerPage(definition: OrderDetailViewController.routeDefinition)
 *
 * // 或者使用扩展简化:
 * extension RouteRegistry {
 *     func registerPage<T: PageRoutable>(_ type: T.Type) {
 *         registerPage(definition: type.routeDefinition)
 *     }
 * }
 * registry.registerPage(OrderDetailViewController.self)
 * ```
 */
class PageRouteDefinition(
    /**
     * 路由模式
     */
    val pattern: String,

    /**
     * 降级配置（可选）
     */
    val fallback: FallbackConfig? = null,

    /**
     * 页面创建工厂
     */
    val factory: (Map<String, Any?>) -> UIViewController
)

/**
 * 注册页面路由（通过 PageRouteDefinition）
 *
 * Swift 使用示例:
 * ```swift
 * registry.registerPage(definition: OrderDetailViewController.routeDefinition)
 * ```
 */
fun RouteRegistry.registerPage(definition: PageRouteDefinition) {
    val creator = IOSPageCreator(definition.factory)
    val config = PageRouteConfig(
        target = PageTarget(creator),
        pageId = null,
        fallback = definition.fallback
    )
    registerPage(definition.pattern, config)
}

/**
 * 批量注册页面路由
 *
 * Swift 使用示例:
 * ```swift
 * registry.registerPages(definitions: [
 *     OrderDetailViewController.routeDefinition,
 *     AccountSettingsViewController.routeDefinition,
 *     PaymentViewController.routeDefinition
 * ])
 * ```
 */
fun RouteRegistry.registerPages(definitions: List<PageRouteDefinition>) {
    definitions.forEach { registerPage(it) }
}
