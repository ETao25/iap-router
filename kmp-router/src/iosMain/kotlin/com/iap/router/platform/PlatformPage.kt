package com.iap.router.platform

import com.iap.router.RouteRegistry
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
        pattern = pattern,
        target = PageTarget(creator),
        pageId = null
    )
    registerPage(config)
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
        pattern = pattern,
        target = PageTarget(creator),
        pageId = pageId
    )
    registerPage(config)
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
 * 用于声明式路由注册，Swift 侧可以定义静态协议桥接此类
 *
 * Swift 使用示例:
 * ```swift
 * // 1. 定义 Swift 协议（静态成员）
 * protocol PageRoutable {
 *     static var pattern: String { get }
 *     static func createPage(params: [String: Any?]) -> UIViewController
 * }
 *
 * extension PageRoutable {
 *     static var routeDefinition: PageRouteDefinition {
 *         PageRouteDefinition(
 *             pattern: pattern,
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
 * ```
 */
class PageRouteDefinition(
    /**
     * 路由模式
     */
    val pattern: String,
    /**
     * 页面创建工厂函数
     */
    val factory: (Map<String, Any?>) -> UIViewController
) {
    /**
     * 可选的页面标识符
     */
    var pageId: String? = null
        private set

    /**
     * 设置自定义 pageId
     */
    fun withPageId(pageId: String): PageRouteDefinition {
        this.pageId = pageId
        return this
    }

    /**
     * 转换为 PageRouteConfig
     */
    fun toConfig(): PageRouteConfig {
        val creator = IOSPageCreator(factory)
        return PageRouteConfig(
            pattern = pattern,
            target = PageTarget(creator),
            pageId = pageId
        )
    }
}

/**
 * 注册页面路由（通过 PageRouteDefinition）
 *
 * Swift 使用示例:
 * ```swift
 * registry.registerPage(definition: OrderDetailViewController.routeDefinition)
 * ```
 */
fun RouteRegistry.registerPage(definition: PageRouteDefinition) {
    registerPage(definition.toConfig())
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
